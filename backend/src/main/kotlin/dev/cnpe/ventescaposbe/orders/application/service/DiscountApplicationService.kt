package dev.cnpe.ventescaposbe.orders.application.service

import dev.cnpe.ventescaposbe.catalog.api.ProductInfoPort
import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.application.dto.request.ApplyDiscountRequest
import dev.cnpe.ventescaposbe.orders.application.dto.response.OrderResponse
import dev.cnpe.ventescaposbe.orders.application.mapper.OrderMapper
import dev.cnpe.ventescaposbe.orders.domain.entity.Order
import dev.cnpe.ventescaposbe.orders.domain.enums.OrderStatus
import dev.cnpe.ventescaposbe.orders.infrastructure.persistence.OrderRepository
import dev.cnpe.ventescaposbe.promotions.application.api.PromotionInfoPort
import dev.cnpe.ventescaposbe.promotions.application.api.dto.ItemContextData
import dev.cnpe.ventescaposbe.promotions.application.api.dto.OrderContextData
import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountRuleType
import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountRuleType.*
import dev.cnpe.ventescaposbe.shared.application.exception.createInvalidDataException
import dev.cnpe.ventescaposbe.shared.application.exception.createInvalidStateException
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

@Service
@Transactional
class DiscountApplicationService(
    private val orderRepository: OrderRepository,
    private val promotionInfoPort: PromotionInfoPort,
    private val orderMapper: OrderMapper,
    private val moneyFactory: MoneyFactory,
    private val productInfoPort: ProductInfoPort

) {
    /**
     * Applies a discount rule to a specific order item.
     */
    fun applyItemDiscount(orderId: Long, itemId: Long, request: ApplyDiscountRequest): OrderResponse {
        log.info { "Attempting to apply discount rule ${request.discountRuleId} to Item ID $itemId in Order ID $orderId" }

        val order = orderRepository.findByIdWithItems(orderId)
            ?: throw createResourceNotFoundException("Order", orderId)

        require(order.status == OrderStatus.PENDING) { "Discounts can only be applied to PENDING orders." }

        val item = order.orderItems.find { it.id == itemId }
            ?: throw createResourceNotFoundException("OrderItem in Order $orderId", itemId)


        require(item.appliedDiscountRuleId == null) {
            "Item $itemId already has discount (Rule ID: ${item.appliedDiscountRuleId}) applied. Remove existing discount first."
            throw createInvalidStateException(
                reason = "ITEM_ALREADY_DISCOUNTED",
                entityId = item.appliedDiscountRuleId,
            )
        }

        val orderContext = buildOrderContextData(order)

        val discountResult = promotionInfoPort.getManualDiscountApplication(request.discountRuleId, orderContext)
            ?: throw createInvalidDataException(
                field = "discountRuleId",
                value = request.discountRuleId,
                parameters = arrayOf(request.discountRuleId.toString())
            )

        require(discountResult.type == ITEM_PERCENTAGE || discountResult.type == ITEM_FIXED_AMOUNT) {
            "Discount rule ${request.discountRuleId} is type ${discountResult.type}, cannot be applied directly to an item."
        }

        val calculatedDiscountAmount = calculateDiscountValue(
            type = discountResult.type,
            value = discountResult.value,
            baseAmount = item.calculateTotalPrice(),
            currencyCode = order.finalAmount.currencyCode
        )

        item.applyDiscount(calculatedDiscountAmount, request.discountRuleId)

        order.recalculateTotals()

        val updatedOrder = orderRepository.save(order)
        log.info { "Applied discount ${discountResult.description} to item $itemId. New order total: ${updatedOrder.finalAmount}" }
        return orderMapper.toResponse(updatedOrder)
    }

    /**
     * Removes any discount applied to a specific order item.
     */
    fun removeItemDiscount(orderId: Long, itemId: Long): OrderResponse {
        log.info { "Attempting to remove discount from Item ID $itemId in Order ID $orderId" }

        val order = orderRepository.findByIdWithItems(orderId)
            ?: throw createResourceNotFoundException("Order", orderId)
        require(order.status == OrderStatus.PENDING) { "Discounts can only be modified on PENDING orders." }

        val item = order.orderItems.find { it.id == itemId }
            ?: throw createResourceNotFoundException("OrderItem in Order $orderId", itemId)

        if (item.appliedDiscountRuleId == null) {
            log.info { "No discount applied to item $itemId. No action taken." }
            return orderMapper.toResponse(order)
        }

        item.removeDiscount()
        order.recalculateTotals()

        val updatedOrder = orderRepository.save(order)
        log.info { "Removed discount from item $itemId. New order total: ${updatedOrder.finalAmount}" }
        return orderMapper.toResponse(updatedOrder)
    }


    /**
     * Applies a discount rule to the entire order.
     */
    fun applyOrderDiscount(orderId: Long, request: ApplyDiscountRequest): OrderResponse {
        log.info { "Attempting to apply discount rule ${request.discountRuleId} to Order ID $orderId" }

        val order = orderRepository.findByIdWithItems(orderId)
            ?: throw createResourceNotFoundException("Order", orderId)
        require(order.status == OrderStatus.PENDING) { "Discounts can only be applied to PENDING orders." }

        require(order.appliedOrderDiscountRuleId == null) {
            "Cannot apply another order-level discount. Remove existing discount (Rule ID: ${order.appliedOrderDiscountRuleId}) first."
        }

        val orderContext = buildOrderContextData(order)
        val discountResult = promotionInfoPort.getManualDiscountApplication(request.discountRuleId, orderContext)
            ?: throw createInvalidDataException(
                field = "discountRuleId",
                value = request.discountRuleId,
                parameters = arrayOf(request.discountRuleId.toString())
            )

        require(discountResult.type == ORDER_PERCENTAGE || discountResult.type == ORDER_FIXED_AMOUNT) {
            "Discount rule ${request.discountRuleId} is type ${discountResult.type}, cannot be applied directly to the order total."
        }

        val totalPreOrderDiscount = order.calculateTotalPreOrderDiscount()
        val calculatedDiscountAmount = calculateDiscountValue(
            discountResult.type,
            discountResult.value,
            totalPreOrderDiscount,
            order.finalAmount.currencyCode
        )

        order.applyOrderDiscount(
            calculatedDiscountAmount,
            request.discountRuleId
        )

        val updatedOrder = orderRepository.save(order)
        log.info { "Applied order discount ${discountResult.description}. New order total: ${updatedOrder.finalAmount}" }
        return orderMapper.toResponse(updatedOrder)
    }

    /**
     * Removes any order-level discount applied to the order.
     */
    fun removeOrderDiscount(orderId: Long): OrderResponse {
        log.info { "Attempting to remove order-level discount from Order ID $orderId" }

        val order = orderRepository.findByIdWithItems(orderId)
            ?: throw createResourceNotFoundException("Order", orderId)
        require(order.status == OrderStatus.PENDING) { "Discounts can only be modified on PENDING orders." }

        if (order.appliedOrderDiscountRuleId == null) {
            log.info { "No order-level discount applied to order $orderId. No action taken." }
            return orderMapper.toResponse(order)
        }

        order.removeOrderDiscount()

        val updatedOrder = orderRepository.save(order)
        log.info { "Removed order-level discount. New order total: ${updatedOrder.finalAmount}" }
        return orderMapper.toResponse(updatedOrder)
    }


    // *******************************
    // 🔰 Private Helpers
    // *******************************


    /** Builds the context DTO needed by PromotionInfoPort */
    private fun buildOrderContextData(order: Order): OrderContextData {
        val itemContextList = order.orderItems.map { item ->
            val productSaleInfo = productInfoPort.getProductSaleInfo(item.productId)
            ItemContextData(
                productId = item.productId,
                categoryId = productSaleInfo.categoryId,
                brandId = productSaleInfo.brandId,
                quantity = item.quantity,
                originalUnitPrice = item.unitPrice,
                currentItemTotalPrice = item.calculateTotalPrice()
            )
        }
        return OrderContextData(
            branchId = order.branchId,
            customerId = order.customerId,
            currentItems = itemContextList,
            currentTotalAmountPreDiscount = order.calculateTotalPreOrderDiscount()
        )
    }

    /** Calculates the monetary value of a discount */
    private fun calculateDiscountValue(
        type: DiscountRuleType,
        value: BigDecimal,
        baseAmount: Money,
        currencyCode: String
    ): Money {
        val zero = moneyFactory.zero(currencyCode)
        if (baseAmount <= zero) return zero

        return when (type) {
            ITEM_PERCENTAGE, ORDER_PERCENTAGE -> {
                val percentage = value.movePointLeft(2)
                baseAmount.times(percentage)
            }

            ITEM_FIXED_AMOUNT, ORDER_FIXED_AMOUNT -> {
                val fixedDiscount = moneyFactory.createMoney(value, currencyCode)
                if (fixedDiscount > baseAmount) baseAmount else fixedDiscount
            }
        }
    }
}