package dev.cnpe.ventescaposbe.orders.application.mapper

import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.customers.application.api.dto.CustomerBasicInfo
import dev.cnpe.ventescaposbe.orders.application.dto.response.OrderItemResponse
import dev.cnpe.ventescaposbe.orders.application.dto.response.OrderResponse
import dev.cnpe.ventescaposbe.orders.application.dto.response.OrderSummaryResponse
import dev.cnpe.ventescaposbe.orders.application.dto.response.PaymentResponse
import dev.cnpe.ventescaposbe.orders.domain.entity.Order
import dev.cnpe.ventescaposbe.orders.domain.entity.OrderItem
import dev.cnpe.ventescaposbe.orders.domain.entity.Payment
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import org.springframework.stereotype.Component

@Component
class OrderMapper(
    private val moneyFactory: MoneyFactory
) {

    /**
     * Converts an Order entity to OrderResponse DTO, calculating total paid and amount due.
     * @param order The order entity to convert
     * @return OrderResponse containing order details with calculated totals
     */
    fun toResponse(order: Order, customerInfo: CustomerBasicInfo? = null): OrderResponse {
        val totalPaid = order.calculateTotalPaid()
        val finalAmount = order.finalAmount

        val amountDue = (finalAmount - totalPaid).let { difference ->
            if (difference.isPositive()) difference else moneyFactory.zero(finalAmount.currencyCode)
        }

        val changeDue = (totalPaid - finalAmount).let { difference ->
            if (difference.isPositive()) difference else null
        }


        return OrderResponse(
            id = order.id!!,
            orderNumber = order.orderNumber,
            status = order.status,
            branchId = order.branchId,
            userIdpId = order.userIdpId,
            customerId = order.customerId,
            customerInfo = customerInfo,
            orderTimestamp = order.orderTimestamp,
            items = order.orderItems.map { toItemResponse(it) },
            payments = order.payments.map { toPaymentResponse(it) },
            subTotal = order.subTotal,
            taxAmount = order.taxAmount,
            totalAmount = order.totalAmount,
            discountAmount = order.discountAmount,
            finalAmount = finalAmount,
            totalPaid = totalPaid,
            amountDue = amountDue,
            changeDue = changeDue,
            notes = order.notes,
            auditData = ResourceAuditData.fromBaseEntity(order)
        )

    }

    /**
     * Converts an OrderItem entity to OrderItemResponse DTO.
     * @param item The order item entity to convert
     * @return OrderItemResponse containing item details with calculated prices
     */
    fun toItemResponse(item: OrderItem): OrderItemResponse {
        return OrderItemResponse(
            id = item.id!!,
            productId = item.productId,
            productName = item.productNameSnapshot,
            sku = item.skuSnapshot,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            netUnitPrice = item.netUnitPrice,
            totalPrice = item.calculateTotalPrice(),
            totalNetPrice = item.calculateTotalNetPrice(),
            discountAmount = item.discountAmount,
            finalPrice = item.calculateFinalPrice()
        )
    }

    /**
     * Converts a Payment entity to PaymentResponse DTO.
     * @param payment The payment entity to convert
     * @return PaymentResponse containing payment details
     */
    fun toPaymentResponse(payment: Payment): PaymentResponse {
        return PaymentResponse(
            id = payment.id!!,
            paymentMethod = payment.paymentMethod,
            amount = payment.amount,
            paymentTimestamp = payment.paymentTimestamp,
            status = payment.status,
            transactionReference = payment.transactionReference
        )
    }

    /**
     * Converts an Order entity to OrderSummaryResponse DTO.
     * Note: Assumes order.orderItems is loaded if needed for itemCount,
     * otherwise, modify to accept itemCount as a parameter if fetched separately.
     * @param order The order entity to convert.
     * @return OrderSummaryResponse containing summary details.
     */
    fun toSummaryResponse(order: Order): OrderSummaryResponse {
        val itemCount = try {
            order.orderItems.size
        } catch (e: org.hibernate.LazyInitializationException) {
            0
        }

        return OrderSummaryResponse(
            id = order.id!!,
            orderNumber = order.orderNumber,
            status = order.status,
            branchId = order.branchId,
            userIdpId = order.userIdpId,
            orderTimestamp = order.orderTimestamp,
            finalAmount = order.finalAmount,
            itemCount = itemCount
        )

    }

}