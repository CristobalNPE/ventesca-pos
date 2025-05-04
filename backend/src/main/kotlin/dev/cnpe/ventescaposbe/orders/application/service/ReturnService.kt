package dev.cnpe.ventescaposbe.orders.application.service

import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.application.dto.request.ProcessReturnRequest
import dev.cnpe.ventescaposbe.orders.application.dto.response.ReturnTransactionResponse
import dev.cnpe.ventescaposbe.orders.application.dto.response.ReturnableItemInfo
import dev.cnpe.ventescaposbe.orders.application.mapper.ReturnMapper
import dev.cnpe.ventescaposbe.orders.domain.entity.Order
import dev.cnpe.ventescaposbe.orders.domain.entity.OrderItem
import dev.cnpe.ventescaposbe.orders.domain.entity.ReturnTransaction
import dev.cnpe.ventescaposbe.orders.domain.entity.ReturnedItem
import dev.cnpe.ventescaposbe.orders.domain.enums.OrderStatus.COMPLETED
import dev.cnpe.ventescaposbe.orders.domain.enums.OrderStatus.REFUNDED
import dev.cnpe.ventescaposbe.orders.event.ItemAdjustmentInfo
import dev.cnpe.ventescaposbe.orders.event.ReturnProcessedEvent
import dev.cnpe.ventescaposbe.orders.infrastructure.persistence.OrderRepository
import dev.cnpe.ventescaposbe.orders.infrastructure.persistence.ReturnTransactionRepository
import dev.cnpe.ventescaposbe.security.context.UserContext
import dev.cnpe.ventescaposbe.sessions.application.api.SessionInfoPort
import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode.INSUFFICIENT_CONTEXT
import dev.cnpe.ventescaposbe.shared.application.exception.createInvalidDataException
import dev.cnpe.ventescaposbe.shared.application.exception.createInvalidStateException
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

private val log = KotlinLogging.logger {}

@Service
@Transactional
class ReturnService(
    private val orderRepository: OrderRepository,
    private val userContext: UserContext,
    private val moneyFactory: MoneyFactory,
    private val returnTransactionRepository: ReturnTransactionRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val returnMapper: ReturnMapper,
    private val sessionInfoPort: SessionInfoPort
) {


    /**
     * Retrieves a list of items from a completed order that are eligible for return.
     */
    @Transactional(readOnly = true)
    fun getReturnableItems(originalOrderId: Long): List<ReturnableItemInfo> {
        log.debug { "Fetching returnable items for original Order ID: $originalOrderId" }

        val order = orderRepository.findByIdWithItems(originalOrderId)
            ?: throw createResourceNotFoundException("Order", originalOrderId)

        require(order.status == COMPLETED || order.status == REFUNDED) {
            "Can only process returns for COMPLETED or already REFUNDED orders. Order $originalOrderId status is ${order.status}."
        }

        return order.orderItems.mapNotNull { item ->
            val returnableQuantity = item.getReturnableQuantity()
            if (returnableQuantity > 0) {
                ReturnableItemInfo(
                    originalOrderItemId = item.id!!,
                    productId = item.productId,
                    productName = item.productNameSnapshot,
                    sku = item.skuSnapshot,
                    originalQuantity = item.quantity,
                    alreadyReturnedQuantity = item.returnedQuantity,
                    returnableQuantity = returnableQuantity,
                    finalUnitPricePaid = calculateFinalUnitPricePaid(item)
                )
            } else null
        }
    }

    /**
     * Processes a return request, creating return records and updating original order items.
     */
    fun processReturn(request: ProcessReturnRequest): ReturnTransactionResponse {
        val userId = userContext.userId
            ?: throw DomainException(errorCode = INSUFFICIENT_CONTEXT, message = "User ID missing.")

        log.info { "Processing return for Order ID: ${request.originalOrderId} by User: $userId" }

        val originalOrder = orderRepository.findByIdWithItems(request.originalOrderId)
            ?: throw createResourceNotFoundException("Order", request.originalOrderId)

        require(originalOrder.status == COMPLETED || originalOrder.status == REFUNDED) {
            "Cannot process return for Order ${request.originalOrderId}. " +
                    "Status is ${originalOrder.status} (must be COMPLETED or REFUNDED)."
        }

        val branchId = originalOrder.branchId

        val openSessionInfo = (sessionInfoPort.findOpenSession(userId, branchId)
            ?: throw createInvalidStateException(
                reason = "NO_OPEN_SESSION",
                entityId = request.originalOrderId,
                additionalDetails = mapOf("userId" to userId, "branchId" to branchId)
            ))
        val currentSessionId = openSessionInfo.sessionId
        log.debug { "Return for Order ID ${request.originalOrderId} will be linked to Session ID $currentSessionId" }

        validateReturnRequestItems(request, originalOrder)

        val currencyCode = originalOrder.finalAmount.currencyCode
        val zero = moneyFactory.zero(currencyCode)

        val returnTransaction = ReturnTransaction(
            originalOrderId = originalOrder.id!!,
            originalOrderNumber = originalOrder.orderNumber,
            branchId = branchId,
            userIdpId = userId,
            customerId = originalOrder.customerId,
            totalRefundAmount = zero,
            refundMethod = request.refundMethod,
            notes = request.notes,
            sessionId = currentSessionId,
        )

        val itemsToRestock = mutableListOf<ItemAdjustmentInfo>()
        val itemsToDiscard = mutableListOf<ItemAdjustmentInfo>()

        // process each returned item:
        request.items.forEach { itemDetail ->
            val originalItem = originalOrder.orderItems.find { it.id == itemDetail.originalOrderItemId }
                ?: throw createResourceNotFoundException(
                    "OrderItem in Order ${request.originalOrderId}",
                    itemDetail.originalOrderItemId
                )

            val unitRefundAmount = calculateFinalUnitPricePaid(originalItem)
            val totalItemRefund = unitRefundAmount.times(itemDetail.quantityToReturn.toBigDecimal())

            val returnedItem = ReturnedItem(
                returnTransaction = returnTransaction,
                originalOrderItemId = originalItem.id!!,
                productId = originalItem.productId,
                productNameSnapshot = originalItem.productNameSnapshot,
                skuSnapshot = originalItem.skuSnapshot,
                quantityReturned = itemDetail.quantityToReturn,
                unitRefundAmount = unitRefundAmount,
                totalItemRefundAmount = totalItemRefund,
                reason = itemDetail.reason,
                restock = itemDetail.restock,
            )
            returnTransaction.addReturnedItem(returnedItem)

            originalItem.returnedQuantity += itemDetail.quantityToReturn
            log.debug { "Updated original OrderItem ${originalItem.id}: returnedQuantity is now ${originalItem.returnedQuantity}" }

            val adjustmentInfo = ItemAdjustmentInfo(originalItem.productId, itemDetail.quantityToReturn)
            if (itemDetail.restock) {
                itemsToRestock.add(adjustmentInfo)
            } else {
                itemsToDiscard.add(adjustmentInfo)
            }
        }

        originalOrder.updateStatus(REFUNDED)
        orderRepository.save(originalOrder)
        log.info { "Original Order ${originalOrder.id} status set to REFUNDED and items updated." }

        val savedReturnTransaction = returnTransactionRepository.save(returnTransaction)
        log.info { "Saved new ReturnTransaction ID: ${savedReturnTransaction.id} linked to Session ID ${savedReturnTransaction.sessionId}." }

        try {
            val event = ReturnProcessedEvent(
                returnTransactionId = savedReturnTransaction.id!!,
                branchId = branchId,
                itemsToRestock = itemsToRestock,
                itemsToDiscard = itemsToDiscard
            )
            eventPublisher.publishEvent(event)
            log.info { "Published ReturnProcessedEvent for Return ID: ${savedReturnTransaction.id}" }
        } catch (e: Exception) {
            log.error(e) { "Failed to publish ReturnProcessedEvent for Return ID: ${savedReturnTransaction.id}. Return remains processed." }
            // retry/dead-letter?
        }

        return returnMapper.toResponse(savedReturnTransaction)
    }

    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun validateReturnRequestItems(request: ProcessReturnRequest, order: Order) {
        val orderItemMap = order.orderItems.associateBy { it.id!! }

        if (request.items.isEmpty()) {
            throw createInvalidDataException(field = "items")
        }

        request.items.forEach { itemDetail ->
            val originalItem = orderItemMap[itemDetail.originalOrderItemId]
                ?: throw createInvalidDataException(
                    field = "items[${itemDetail.originalOrderItemId}].originalOrderItemId",
                    value = itemDetail.originalOrderItemId,
                )

            if (itemDetail.quantityToReturn <= 0) {
                throw createInvalidDataException(
                    field = "items[${itemDetail.originalOrderItemId}].quantityToReturn",
                    value = itemDetail.quantityToReturn,
                )
            }

            val returnableQuantity = originalItem.getReturnableQuantity()
            if (itemDetail.quantityToReturn > returnableQuantity) {
                throw createInvalidDataException(
                    field = "items[${itemDetail.originalOrderItemId}].quantityToReturn",
                    value = itemDetail.quantityToReturn,
                )
            }
            // TODO:  check for UnitType? (can't return 1.5 if unit is UNIT?)
        }
        log.debug { "Validation passed for return request items." }
    }

    private fun calculateFinalUnitPricePaid(item: OrderItem): Money {
        if (item.quantity == 0.0) {
            return item.unitPrice.copy(amount = BigDecimal.ZERO)
        }
        val finalLinePrice = item.calculateFinalPrice()

        val unitPriceAmount = finalLinePrice.amount.divide(item.quantity.toBigDecimal(), 10, RoundingMode.HALF_UP)
        return finalLinePrice.copy(
            amount = unitPriceAmount.setScale(
                finalLinePrice.amount.scale(),
                RoundingMode.HALF_UP
            )
        )
    }
}