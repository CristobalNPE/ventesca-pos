package dev.cnpe.ventescaposbe.orders.application.service

import dev.cnpe.ventescaposbe.business.application.api.BusinessDataPort
import dev.cnpe.ventescaposbe.business.application.dto.response.BusinessBranchInfo
import dev.cnpe.ventescaposbe.customers.application.api.CustomerInfoPort
import dev.cnpe.ventescaposbe.orders.application.dto.response.*
import dev.cnpe.ventescaposbe.orders.domain.enums.OrderStatus
import dev.cnpe.ventescaposbe.orders.infrastructure.persistence.OrderRepository
import dev.cnpe.ventescaposbe.security.ports.IdentityProviderPort
import dev.cnpe.ventescaposbe.shared.application.exception.createInvalidStateException
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import dev.cnpe.ventescaposbe.shared.domain.vo.Address
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class ReceiptService(
    private val orderRepository: OrderRepository,
    private val businessDataPort: BusinessDataPort,
    private val customerInfoPort: CustomerInfoPort,
    private val identityProviderPort: IdentityProviderPort
) {

    fun generateReceiptData(orderId: Long): ReceiptResponse {
        log.debug { "Generating receipt data for Order ID: $orderId" }

        val order = orderRepository.findByIdWithItemsAndPayments(orderId)
            ?: throw createResourceNotFoundException("Order", orderId)

        require(order.status == OrderStatus.COMPLETED || order.status == OrderStatus.REFUNDED) {
            throw createInvalidStateException(
                reason = "INVALID_ORDER_STATUS_FOR_RECEIPT",
                entityId = orderId,
                additionalDetails = mapOf(
                    "status" to order.status,
                    "allowed" to "${OrderStatus.COMPLETED}, ${OrderStatus.REFUNDED}"
                )
            )
        }

        val businessName = businessDataPort.getCurrentBusinessName() ?: "Ventesca POS"
        val branchDetails = fetchBranchDetails(order.branchId)

        val customerBasicInfo = order.customerId?.let { customerInfoPort.getCustomerBasicInfo(it) }
        val cashierName = fetchCashierName(order.userIdpId)

        val header = ReceiptHeader(
            businessName = businessName,
            branchName = branchDetails?.branchName ?: "${order.branchId}",
            branchAddress = formatAddress(branchDetails?.address),
            branchPhone = branchDetails?.contactNumber,
            orderNumber = order.orderNumber,
            orderTimestamp = order.orderTimestamp,
            cashierId = order.userIdpId,
            cashierName = cashierName,
            sessionNumber = null
        )

        val receiptCustomerInfo = customerBasicInfo?.let {
            ReceiptCustomerInfo(
                customerId = it.id,
                fullName = it.fullName,
                taxId = null // fixme: customerDetails?.taxId  ??
            )
        }

        val items = order.orderItems.map { item ->
            ReceiptLineItem(
                quantity = item.quantity,
                productName = item.productNameSnapshot,
                sku = item.skuSnapshot,
                unitPrice = item.unitPrice,
                lineTotal = item.calculateTotalPrice(),
                discountApplied = item.discountAmount.takeIf { it.amount > BigDecimal.ZERO },
            )
        }

        val totalPaid = order.calculateTotalPaid()
        val changeDue = (totalPaid - order.finalAmount).let { difference ->
            if (difference.isPositive()) difference else null
        }

        val totals = ReceiptTotals(
            subTotal = order.subTotal,
            taxAmount = order.taxAmount,
            grossTotal = order.totalAmount,
            discountAmount = order.discountAmount + order.orderLevelDiscountAmount,
            finalAmount = order.finalAmount,
            totalPaid = totalPaid,
            changeDue = changeDue
        )

        val payments = order.payments.map { payment ->
            ReceiptPayment(
                method = payment.paymentMethod.name,
                amount = payment.amount,
                transactionReference = payment.transactionReference
            )
        }

        val footer = ReceiptFooter(
            message = businessDataPort.getCurrentBusinessBrandMessage()
                ?: "Thank you for your business!",//fixme: get placeholder from MessageSource
            returnPolicy = "Returns accepted within 30 days with receipt" //fixme: this is placeholder, need to add a field in business, and externalize the msg
        )

        log.info { "Successfully generated receipt data for Order ID: $orderId" }
        return ReceiptResponse(
            header = header,
            customerInfo = receiptCustomerInfo,
            items = items,
            totals = totals,
            payments = payments,
            footer = footer
        )
    }

    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun fetchCashierName(userIdpId: String): String? {
        return try {
            identityProviderPort.findUserById(userIdpId)?.let { userInfo ->
                listOfNotNull(userInfo.firstName, userInfo.lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .ifEmpty { userInfo.username ?: userInfo.email }
            }
        } catch (e: Exception) {
            log.warn(e) { "Could not fetch cashier name for user ID $userIdpId" }
            null
        }
    }

    private fun fetchBranchDetails(branchId: Long): BusinessBranchInfo? {
        return businessDataPort.getBranchDetails(branchId)
    }

    private fun formatAddress(address: Address?): String? {
        if (address == null) return null
        return listOfNotNull(address.street, address.city, address.postalCode, address.country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .takeIf { it.isNotEmpty() }
    }

}