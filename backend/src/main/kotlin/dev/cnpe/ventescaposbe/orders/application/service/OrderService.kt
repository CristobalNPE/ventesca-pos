package dev.cnpe.ventescaposbe.orders.application.service

import dev.cnpe.ventescaposbe.business.application.api.BusinessDataPort
import dev.cnpe.ventescaposbe.catalog.api.ProductInfoPort
import dev.cnpe.ventescaposbe.catalog.application.util.ProductUtils
import dev.cnpe.ventescaposbe.catalog.domain.enums.ProductStatus
import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.inventory.application.api.InventoryInfoPort
import dev.cnpe.ventescaposbe.inventory.application.api.dto.BranchInventoryDetails
import dev.cnpe.ventescaposbe.orders.application.dto.request.AddItemToOrderRequest
import dev.cnpe.ventescaposbe.orders.application.dto.request.AddPaymentRequest
import dev.cnpe.ventescaposbe.orders.application.dto.request.UpdateOrderItemQuantityRequest
import dev.cnpe.ventescaposbe.orders.application.dto.response.OrderResponse
import dev.cnpe.ventescaposbe.orders.application.dto.response.OrderSummaryResponse
import dev.cnpe.ventescaposbe.orders.application.mapper.OrderMapper
import dev.cnpe.ventescaposbe.orders.domain.entity.Order
import dev.cnpe.ventescaposbe.orders.domain.entity.OrderItem
import dev.cnpe.ventescaposbe.orders.domain.entity.Payment
import dev.cnpe.ventescaposbe.orders.domain.enums.OrderStatus
import dev.cnpe.ventescaposbe.orders.domain.enums.PaymentStatus
import dev.cnpe.ventescaposbe.orders.event.ItemSoldInfo
import dev.cnpe.ventescaposbe.orders.event.OrderCompletedEvent
import dev.cnpe.ventescaposbe.orders.infrastructure.persistence.OrderRepository
import dev.cnpe.ventescaposbe.security.context.UserContext
import dev.cnpe.ventescaposbe.shared.application.dto.PageResponse
import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode.INSUFFICIENT_CONTEXT
import dev.cnpe.ventescaposbe.shared.application.exception.createInvalidStateException
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import dev.cnpe.ventescaposbe.shared.application.service.CodeGeneratorService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.criteria.Predicate
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private val log = KotlinLogging.logger {}

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val productInfoPort: ProductInfoPort,
    private val orderMapper: OrderMapper,
    private val userContext: UserContext,
    private val businessDataPort: BusinessDataPort,
    private val inventoryInfoPort: InventoryInfoPort,
    private val moneyFactory: MoneyFactory,
    private val codeGeneratorService: CodeGeneratorService,
    private val productUtils: ProductUtils,
    private val eventPublisher: ApplicationEventPublisher
) {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yydMMdd")
        private const val ORDER_PREFIX = "V"

    }

    /**
     * Starts a new empty order for the current user and branch.
     * @param branchId The ID of the branch where the order is being created.
     * @return A response DTO representing the newly created pending order.
     */
    fun startNewOrder(branchId: Long): OrderResponse {
        val userId = userContext.userId
            ?: throw DomainException(
                errorCode = INSUFFICIENT_CONTEXT,
                message = "User ID not available in context."
            )
        val allowedBranches = userContext.allowedBranchIds
            ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "Allowed branches not available in user context.")

        require(!allowedBranches.contains(branchId)) {
            "User $userId is not authorized to start orders for branch $branchId. Allowed: $allowedBranches"
            throw DomainException(
                errorCode = GeneralErrorCode.OPERATION_NOT_ALLOWED,
                details = mapOf(
                    "userId" to userId,
                    "requestedBranchId" to branchId,
                    "allowedBranches" to allowedBranches
                )
            )
        }

        log.info { "Starting new order by User: $userId in Branch: $branchId" }

        val businessPaymentData = businessDataPort.getBusinessPaymentData()
        val currencyCode = businessPaymentData.currencyCode
        val zeroAmount = moneyFactory.zero(currencyCode)

        val newOrder = Order(
            orderNumber = generateOrderNumber(),
            status = OrderStatus.PENDING,
            branchId = branchId,
            userIdpId = userId,
            orderTimestamp = OffsetDateTime.now(),
            subTotal = zeroAmount,
            taxAmount = zeroAmount,
            totalAmount = zeroAmount,
            discountAmount = zeroAmount,
            finalAmount = zeroAmount
        )

        val savedOrder = orderRepository.save(newOrder)
        log.info { "New order created with ID: ${savedOrder.id}, Number: ${savedOrder.orderNumber}" }
        return orderMapper.toResponse(savedOrder)
    }

    /**
     * Adds a product item to an existing pending order.
     * Validates stock availability before adding.
     * Recalculates order totals.
     *
     * @param orderId The ID of the pending order.
     * @param request DTO containing product ID and quantity.
     * @return The updated order details.
     */
    fun addItem(orderId: Long, request: AddItemToOrderRequest): OrderResponse {
        log.debug { "Attempting to add item (Product ID: ${request.productId}, Qty: ${request.quantity}) to Order ID: $orderId" }

        val order = orderRepository.findByIdWithItems(orderId)
            ?: throw createResourceNotFoundException("Order", orderId)

        require(order.status == OrderStatus.PENDING) {
            "Cannot add items to order ${order.id} because its status is ${order.status}"
        }

        val (productId, productName, productSku, status, currentSellingPrice) = productInfoPort.getProductSaleInfo(
            request.productId
        )

        require(status === ProductStatus.ACTIVE) {
            "Product $productId is not ACTIVE and cannot be added to the order."
        }

        val currentPrice = currentSellingPrice
            ?: throw DomainException(
                errorCode = GeneralErrorCode.INVALID_STATE,
                details = mapOf("productId" to productId, "reason" to "MISSING_ACTIVE_PRICE"),
                message = "Product $productId has no active price."
            )

        val branchInventory = retrieveProductInventoryForBranch(request.productId, order.branchId)

        if (branchInventory.currentQuantity < request.quantity) {
//            //FIXME: Should we still add if no enough quantity? In that case just log a warning? Or notify inconsistency to the front client?
//            //Ideally, we don't want to stop the sale, because this POS is meant mainly for people buying 'on-the-spot' so if they bring an item to the counter, we need to sell it even if the system says 'no stock' for one reason or other.
//            //Having that in mind, what would be the best option here?
//            throw DomainException(
//                INSUFFICIENT_STOCK,
//                details = mapOf(
//                    "productId" to request.productId,
//                    "branchId" to order.branchId,
//                    "requested" to request.quantity,
//                    "available" to branchInventory.currentQuantity
//                ),
//            )

            log.warn {
                "Not enough stock registered for Product ID: ${request.productId} in Branch ${order.branchId}. " +
                        "Available: ${branchInventory.currentQuantity}, Requested: ${request.quantity}." +
                        "Sale will still proceed."
            }
            // FIXME: should we add a flag/note to the order or item if needed for reporting later?
        }

        // create OrderItem
        val businessPaymentData = businessDataPort.getBusinessPaymentData()
        val netUnitPrice = productUtils.calculateNetPrice(currentPrice.amount, businessPaymentData)
        val zeroDiscount = moneyFactory.zero(currentPrice.currencyCode)

        val newItem = OrderItem(
            order = order,
            productId = productId,
            productNameSnapshot = productName,
            skuSnapshot = productSku,
            quantity = request.quantity,
            unitPrice = currentSellingPrice,
            netUnitPrice = Money(netUnitPrice, currentPrice.currencyCode),
            discountAmount = zeroDiscount
        )

        order.addItem(newItem) // recalculates totals internally

        val updatedOrder = orderRepository.save(order)
        log.info { "Item (Product ID: ${request.productId}) added to Order ID: $orderId. New total: ${updatedOrder.finalAmount}" }

        return orderMapper.toResponse(updatedOrder)
    }


    /**
     * Updates the quantity of a specific item within a pending order.
     * Checks inventory if quantity increases, but proceeds even if insufficient (logs warning).
     * Recalculates order totals.
     *
     * @param orderId The ID of the pending order.
     * @param orderItemId The ID of the order item to update.
     * @param request DTO containing the new quantity.
     * @return The updated order details.
     */
    fun updateItemQuantity(orderId: Long, orderItemId: Long, request: UpdateOrderItemQuantityRequest): OrderResponse {
        log.debug { "Attempting to update quantity for Item ID: $orderItemId in Order ID: $orderId to ${request.newQuantity}" }

        val order = orderRepository.findByIdWithItems(orderId)
            ?: throw createResourceNotFoundException("Order", orderId)

        require(order.status == OrderStatus.PENDING) {
            "Cannot update item quantity for order ${order.id} because its status is ${order.status}"
        }

        val itemToUpdate = order.orderItems.find { it.id == orderItemId }
            ?: throw createResourceNotFoundException("OrderItem", orderItemId)

        val oldQuantity = itemToUpdate.quantity
        val newQuantity = request.newQuantity

        if (oldQuantity == newQuantity) {
            log.info { "Quantity for Item ID $orderItemId is already ${newQuantity}. No update needed." }
            return orderMapper.toResponse(order)
        }

        if (newQuantity > oldQuantity) {
            val quantityDifference = newQuantity - oldQuantity
            val branchInventory = retrieveProductInventoryForBranch(itemToUpdate.productId, order.branchId)

            if (branchInventory.currentQuantity < quantityDifference) {
                log.warn {
                    "Insufficient stock to cover quantity increase for Product ID ${itemToUpdate.productId} in Branch ${order.branchId}. " +
                            "Increasing by: $quantityDifference, Available (System): ${branchInventory.currentQuantity}. " +
                            "Proceeding with quantity update."
                }
            }
        }

        itemToUpdate.quantity = newQuantity

        order.recalculateTotals()

        val updatedOrder = orderRepository.save(order)
        log.info {
            "Quantity updated for Item ID $orderItemId in Order ID $orderId. " +
                    "New quantity: $newQuantity. New total: ${updatedOrder.finalAmount}"
        }

        return orderMapper.toResponse(updatedOrder)
    }


    /**
     * Removes a specific item from a pending order.
     * Recalculates order totals after removal.
     *
     * @param orderId The ID of the pending order.
     * @param orderItemId The ID of the order item to remove.
     * @return The updated order details after item removal.
     * @throws DomainException(RESOURCE_NOT_FOUND) if the order doesn't exist.
     * @throws IllegalArgumentException if the item ID is not found within that specific order.
     * @throws IllegalStateException if the order is not in PENDING status.
     */
    fun removeItem(orderId: Long, orderItemId: Long): OrderResponse {
        log.debug { "Attempting to remove item with ID: $orderItemId from Order ID: $orderId" }

        val order = orderRepository.findByIdWithItems(orderId)
            ?: throw createResourceNotFoundException("Order", orderId)

        require(order.status == OrderStatus.PENDING) {
            "Cannot remove items from order ${order.id} because its status is ${order.status}"
        }

        try {
            order.removeItemById(orderItemId)
        } catch (e: IllegalArgumentException) {
            log.warn { "Attempted to remove item ID $orderItemId which was not found in order ID $orderId." }
            throw createResourceNotFoundException("OrderItem in Order $orderId", orderItemId)
        }

        val updatedOrder = orderRepository.save(order)
        log.info { "Item ID $orderItemId removed from Order ID $orderId. New total: ${updatedOrder.finalAmount}" }

        return orderMapper.toResponse(updatedOrder)
    }

    /**
     * Retrieves the detailed information for a specific order, including its items and payments.
     *
     * @param orderId The ID of the order to retrieve.
     * @return A detailed response DTO for the order.
     * @throws DomainException(RESOURCE_NOT_FOUND) if the order doesn't exist.
     */
    @Transactional(readOnly = true)
    fun getOrderDetails(orderId: Long): OrderResponse {
        log.debug { "Fetching details for Order ID: $orderId" }

        val order = (orderRepository.findByIdWithItemsAndPayments(orderId)
            ?: throw createResourceNotFoundException("Order", orderId))

        return orderMapper.toResponse(order)
    }


    /**
     * Records a payment against an order, assuming external completion.
     * Calculates change if overpayment occurs. Updates order status based on payment completion.
     * Designed to allow future integration with payment gateways.
     *
     * @param orderId The ID of the order to add payment to.
     * @param request DTO containing payment details declared by the cashier.
     * @return The updated order details, potentially including change due.
     * @throws DomainException(RESOURCE_NOT_FOUND) if the order doesn't exist.
     * @throws IllegalStateException if the order status prevents payment (e.g., already COMPLETED or CANCELLED).
     */
    fun addPayment(orderId: Long, request: AddPaymentRequest): OrderResponse {
        log.debug { "Recording payment for Order ID: $orderId - Method: ${request.paymentMethod}, Amount: ${request.amount}" }

        val order = orderRepository.findByIdWithItemsAndPayments(orderId)
            ?: throw createResourceNotFoundException("Order", orderId)

        require(order.status == OrderStatus.PENDING || order.status == OrderStatus.PROCESSING) {
            "Cannot add payments to order ${order.id} because its status is ${order.status}"
        }

        val paymentAmount = moneyFactory.createMoney(request.amount, order.finalAmount.currencyCode)

        // for now we assume external payment is successful FIXME?
        val paymentStatus = PaymentStatus.COMPLETED

        val newPayment = Payment(
            order = order,
            paymentMethod = request.paymentMethod,
            amount = paymentAmount,
            paymentTimestamp = OffsetDateTime.now(),
            status = paymentStatus,
            transactionReference = request.transactionReference,
        )

        order.addPayment(newPayment)
        orderRepository.save(order)
        log.info {
            "Payment record added for Order ID $orderId. " +
                    "Method: ${request.paymentMethod}, " +
                    "Amount: ${paymentAmount}, " +
                    "Status: $paymentStatus"
        }

        val totalPaid = order.calculateTotalPaid()
        val isFullyPaid = totalPaid >= order.finalAmount

        if (isFullyPaid) {
            if (order.status == OrderStatus.PENDING) {
                log.info { "Order ID: $orderId is now fully paid. Updating status from PENDING to PROCESSING." }
                order.updateStatus(OrderStatus.PROCESSING)
                orderRepository.save(order)
            } else {
                log.info { "Order ID: $orderId remains fully paid (was already PROCESSING or paid by this payment)." }
            }

        } else {
            if (order.status == OrderStatus.PENDING) {
                log.info { "Order ID: $orderId is now partially paid. Updating status from PENDING to PROCESSING." }
                order.updateStatus(OrderStatus.PROCESSING)
                orderRepository.save(order)
            } else {
                log.info { "Order ID: $orderId remains partially paid (still PROCESSING)." }
            }
        }

        val finalOrderState = if (order.status == OrderStatus.PROCESSING) {
            orderRepository.findByIdWithItemsAndPayments(orderId)!!
        } else {
            order
        }
        return orderMapper.toResponse(finalOrderState)
    }


    /**
     * Finalizes a fully paid order by marking it as COMPLETED.
     * Publishes an OrderCompletedEvent for other modules (like Inventory).
     *
     * @param orderId The ID of the order to complete.
     * @return The final state of the completed order.
     * @throws DomainException(RESOURCE_NOT_FOUND) if the order doesn't exist.
     * @throws IllegalStateException if the order is not fully paid or not in PROCESSING status.
     */
    fun completeOrder(orderId: Long): OrderResponse {
        log.info { "Attempting to complete Order ID: $orderId" }

        val order = (orderRepository.findByIdWithItemsAndPayments(orderId)
            ?: throw createResourceNotFoundException("Order", orderId))

        require(order.status != OrderStatus.COMPLETED && order.status != OrderStatus.CANCELLED) {
            "Order ${order.id} cannot be completed because its status is already ${order.status}."
            // TODO: consider DomainException(INVALID_STATE)?
        }

        require(order.isFullyPaid()) {
            "Order ${order.id} cannot be completed as it is not fully paid. " +
                    "Amount Due: ${order.finalAmount - order.calculateTotalPaid()}"
            // TODO: consider DomainException(INVALID_STATE)?
        }

        order.updateStatus(OrderStatus.COMPLETED)
        val savedOrder = orderRepository.save(order)
        log.info { "✅ Order ID: $orderId marked as COMPLETED." }

        try {
            val itemsSoldInfo = savedOrder.orderItems.map {
                ItemSoldInfo(productId = it.productId, quantity = it.quantity)
            }

            val event = OrderCompletedEvent(
                orderId = savedOrder.id!!,
                branchId = savedOrder.branchId,
                itemsSold = itemsSoldInfo
            )
            eventPublisher.publishEvent(event)
            log.info { "Published OrderCompletedEvent for Order ID: ${savedOrder.id}" }
        } catch (e: Exception) {
            log.error(e) {
                "Failed to publish OrderCompletedEvent for Order ID: ${savedOrder.id}." +
                        " Order remains COMPLETED."
            }
            //TODO: listener should handle retries ? or be idempotent?
        }
        return orderMapper.toResponse(savedOrder)
    }


    /**
     * Retrieves a paginated list of order summaries, applying optional filters.
     *
     * @param branchId Optional filter by branch ID.
     * @param status Optional filter by order status.
     * @param startDate Optional filter for orders created on or after this date.
     * @param endDate Optional filter for orders created on or before this date.
     * @param pageable Pagination and sorting information.
     * @return A PageResponse containing OrderSummaryResponse objects.
     */
    @Transactional(readOnly = true)
    fun listOrders(
        branchId: Long?,
        status: OrderStatus?,
        startDate: OffsetDateTime?,
        endDate: OffsetDateTime?,
        pageable: Pageable
    ): PageResponse<OrderSummaryResponse> {
        log.debug {
            "Listing orders with filters - branchId: $branchId, status: $status, " +
                    "startDate: $startDate, endDate: $endDate, pageable: $pageable"
        }

        //build spec
        val spec = Specification<Order> { root, query, cb ->
            val predicates = mutableListOf<Predicate>()

            branchId?.let {
                predicates.add(cb.equal(root.get<Long>("branchId"), it))
            }
            status?.let {
                predicates.add(cb.equal(root.get<OrderStatus>("status"), it))
            }
            startDate?.let {
                predicates.add(cb.greaterThanOrEqualTo(root.get<OffsetDateTime>("orderTimestamp"), it))
            }
            endDate?.let {
                // ad 1 day to endDate to make it inclusive of the whole day if only date is provided
                val inclusiveEndDate = it.toLocalDate().plusDays(1).atStartOfDay().atOffset(it.offset)
                predicates.add(cb.lessThan(root.get<OffsetDateTime>("orderTimestamp"), inclusiveEndDate))
            }

            // user can only see orders from branches they have access to
            val allowedBranches = userContext.allowedBranchIds
            val currentUserId = userContext.userId

            if (allowedBranches == null) {
                log.warn { "Allowed branches null in user context for user $currentUserId. Returning empty list." }
                return@Specification cb.disjunction()
            }

            val specificBranchRequested: Long? = branchId
            val requestedBranchIsAllowed =
                specificBranchRequested != null && allowedBranches.contains(specificBranchRequested)

            when {
                // requested a specific branch but not allowed to see it
                specificBranchRequested != null && !requestedBranchIsAllowed -> {
                    log.warn { "User requested branch $specificBranchRequested which is not in their allowed set $allowedBranches. Returning empty list." }
                    predicates.add(cb.disjunction())
                }

                // did not request a specific branch, or requested branch is allowed
                specificBranchRequested == null -> {
                    if (allowedBranches.isNotEmpty()) {
                        predicates.add(root.get<Long>("branchId").`in`(allowedBranches))
                    } else {
                        log.warn { "User $currentUserId has no allowed branches assigned. Returning empty order list." }
                        predicates.add(cb.disjunction())
                    }
                }
            }

            cb.and(*predicates.toTypedArray())
        }

        val orderPage: Page<Order> = orderRepository.findAll(spec, pageable)
        val summaryPage = orderPage.map { orderMapper.toSummaryResponse(it) }

        return PageResponse.from(summaryPage)
    }


    /**
     * Cancels an order if it's in a cancellable state (PENDING or PROCESSING).
     * Sets the order status to CANCELLED.
     *
     * @param orderId The ID of the order to cancel.
     * @return The updated order details with the CANCELLED status.
     * @throws DomainException(RESOURCE_NOT_FOUND) if the order doesn't exist.
     * @throws DomainException(INVALID_STATE) if the order cannot be cancelled (e.g., already completed).
     */
    fun cancelOrder(orderId: Long): OrderResponse {
        log.warn { "Attempting to cancel Order ID: $orderId" }

        val order = orderRepository.findByIdOrNull(orderId)
            ?: throw createResourceNotFoundException("Order", orderId)

        val cancellableStatuses = setOf(OrderStatus.PENDING, OrderStatus.PROCESSING)

        if (order.status !in cancellableStatuses) {
            throw createInvalidStateException(
                reason = "MISSING_ACTIVE_PRICE",
                entityId = orderId,
                additionalDetails = mapOf(
                    "currentStatus" to order.status,
                    "allowedStatuses" to cancellableStatuses.joinToString()
                )
            )
        }

        order.updateStatus(OrderStatus.CANCELLED)

        val savedOrder = orderRepository.save(order)
        log.info { "❌ Order ID: $orderId marked as CANCELLED." }

        return orderMapper.toResponse(savedOrder)
    }


    // *******************************
    // 🔰 Private Helpers
    // *******************************


    private fun generateOrderNumber(): String {

        val datePart = LocalDate.now().format(DATE_FORMATTER)
        val sequenceNumber = try {
            orderRepository.getNextOrderNumberSequenceValue()
        } catch (e: Exception) {
            log.error(e) { "Failed to retrieve next value from order_number_seq. Falling back to UUID." }
            return "ERR-" + UUID.randomUUID().toString()
        }

        val orderNumber = "$ORDER_PREFIX$datePart-$sequenceNumber"

        log.debug { "Generated order number: $orderNumber" }
        return orderNumber
    }

    private fun retrieveProductInventoryForBranch(
        productId: Long,
        branchId: Long
    ): BranchInventoryDetails {
        val branchInventory = try {
            inventoryInfoPort.getBranchInventoryDetails(productId, branchId)
        } catch (e: DomainException) {
            if (e.errorCode == GeneralErrorCode.RESOURCE_NOT_FOUND) {
                log.error { "Inventory item not found for Product ID $productId in Branch ${branchId}. Cannot add to order." }

                throw DomainException(
                    GeneralErrorCode.INVALID_STATE,
                    details = mapOf(
                        "productId" to productId,
                        "branchId" to branchId,
                        "reason" to "INVENTORY_ITEM_MISSING"
                    ),
                    message = "Inventory record missing for product in this branch."
                )
            } else throw e
        }
        return branchInventory
    }


}