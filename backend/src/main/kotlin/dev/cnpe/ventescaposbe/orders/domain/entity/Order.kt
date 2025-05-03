package dev.cnpe.ventescaposbe.orders.domain.entity

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.domain.enums.OrderStatus
import dev.cnpe.ventescaposbe.orders.domain.enums.PaymentStatus
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "orders")
class Order(

    @Column(name = "order_number", nullable = false, unique = true)
    var orderNumber: String, // generated

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "branch_id", nullable = false, updatable = false)
    val branchId: Long,

    @Column(name = "user_idp_id", nullable = false, updatable = false)
    val userIdpId: String,

    @Column(name = "customer_id") // TODO: Link to Customer entity later
    var customerId: Long? = null,

    @Column(name = "order_timestamp", nullable = false)
    val orderTimestamp: OffsetDateTime = OffsetDateTime.now(),

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "sub_total_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "sub_total_currency", nullable = false, length = 3)
        )
    )
    var subTotal: Money,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "tax_amount", nullable = false)),
        AttributeOverride(name = "currencyCode", column = Column(name = "tax_currency", nullable = false, length = 3))
    )
    var taxAmount: Money,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_amount", nullable = false)),
        AttributeOverride(name = "currencyCode", column = Column(name = "total_currency", nullable = false, length = 3))
    )
    var totalAmount: Money,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "discount_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "discount_currency", nullable = false, length = 3)
        )
    )
    var discountAmount: Money,

    @Column(name = "applied_order_discount_rule_id")
    var appliedOrderDiscountRuleId: Long? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "order_level_discount_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "order_level_discount_currency", nullable = false, length = 3)
        )
    )
    var orderLevelDiscountAmount: Money = subTotal.copy(amount = BigDecimal.ZERO),

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "final_amount", nullable = false)),
        AttributeOverride(name = "currencyCode", column = Column(name = "final_currency", nullable = false, length = 3))
    )
    var finalAmount: Money,

    @Column(name = "notes", length = 500)
    var notes: String? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val orderItems: MutableList<OrderItem> = mutableListOf(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val payments: MutableList<Payment> = mutableListOf(),


    id: Long? = null,
    version: Int = 0
) : BaseEntity(id, version) {

    /** Adds an item to the order and recalculates totals. */
    fun addItem(item: OrderItem) {
        require(this.status == OrderStatus.PENDING) { "Cannot add items to an order unless it is PENDING" }
        item.order = this
        this.orderItems.add(item)
        recalculateTotals()
    }

    /** Removes an item from the order and recalculates totals. */
    fun removeItem(itemToRemove: OrderItem) {
        require(this.status == OrderStatus.PENDING) { "Cannot remove items from an order unless it is PENDING" }
        requireNotNull(itemToRemove.id) { "Cannot remove an OrderItem that hasn't been saved (ID is null)" }

        val removed = orderItems.removeIf { it.id == itemToRemove.id }

        if (removed) {
            recalculateTotals()
        } else {
            throw IllegalArgumentException("Item to remove (ID: ${itemToRemove.id}) does not belong to this order (ID: ${this.id})")
        }
    }

    /** Removes an item by its ID from the order and recalculates totals. */
    fun removeItemById(orderItemId: Long) {
        require(this.status == OrderStatus.PENDING) { "Cannot remove items from an order unless it is PENDING" }
        val removed = orderItems.removeIf { it.id == orderItemId }
        if (removed) {
            recalculateTotals()
        } else {
            throw IllegalArgumentException("Item with ID $orderItemId not found in this order (ID: ${this.id})")
        }
    }

    /** Adds a payment to the order. Does not recalculate order totals. */
    fun addPayment(payment: Payment) {
        require(this.status == OrderStatus.PENDING || this.status == OrderStatus.PROCESSING) {
            "Cannot add payments unless order status is PENDING or PROCESSING"
        }
        payment.order = this
        this.payments.add(payment)
        // TODO: Payment completion logic might trigger status changes (e.g., to PROCESSING or COMPLETED)
        // This should likely happen in the service layer after saving the payment.
    }


    /** Recalculates subTotal, taxAmount, totalAmount, and finalAmount based on current orderItems. */
    fun recalculateTotals() {
        if (orderItems.isEmpty()) {
            val zero = zero()
            this.subTotal = zero
            this.taxAmount = zero
            this.totalAmount = zero
            this.discountAmount = zero
            this.finalAmount = zero
            return
        }

        val zero = zero()

        var calculatedSubTotal = zero
        var calculatedTotalAmount = zero
        var calculatedDiscount = zero

        orderItems.forEach { item ->
            item.unitPrice.assertSameCurrency(zero)
            calculatedSubTotal += item.calculateFinalNetPrice()
            calculatedTotalAmount += item.calculateFinalPrice()
            calculatedDiscount += item.discountAmount
        }

        val maxOrderDiscount = calculatedTotalAmount

        if (this.orderLevelDiscountAmount > maxOrderDiscount) {
            log.warn { "Order-level discount ${this.orderLevelDiscountAmount} exceeds item total $maxOrderDiscount for order ${this.id}. Capping." }
            this.orderLevelDiscountAmount = maxOrderDiscount
        }

        this.subTotal = calculatedSubTotal
        this.totalAmount = calculatedTotalAmount
        this.taxAmount = calculatedTotalAmount - calculatedSubTotal
        this.discountAmount = calculatedDiscount
        this.finalAmount = this.totalAmount - this.discountAmount

        require(this.subTotal.isNonNegative()) { "Calculated subTotal cannot be negative" }
        require(this.taxAmount.isNonNegative()) { "Calculated taxAmount cannot be negative" }
        require(this.totalAmount.isNonNegative()) { "Calculated totalAmount cannot be negative" }
        require(this.discountAmount.isNonNegative()) { "Calculated discountAmount cannot be negative" }
        require(this.orderLevelDiscountAmount.isNonNegative()) { "Calculated orderLevelDiscountAmount cannot be negative: ${this.orderLevelDiscountAmount}" }
        require(this.finalAmount.isNonNegative()) { "Calculated finalAmount cannot be negative" }
    }

    /** Calculates the total amount paid so far. */
    fun calculateTotalPaid(): Money {
        if (payments.isEmpty()) {
            return zero()
        }
        val currencyCode = payments.first().amount.currencyCode
        val zero = zero()
        return payments.filter { it.status == PaymentStatus.COMPLETED }
            .fold(zero) { sum, payment -> sum + payment.amount }
    }

    fun isFullyPaid(): Boolean {
        return calculateTotalPaid() >= finalAmount
    }

    /** Changes the order status, ensuring valid transitions. */
    fun updateStatus(newStatus: OrderStatus) {

        if ((this.status == OrderStatus.COMPLETED ||
                    this.status == OrderStatus.CANCELLED ||
                    this.status == OrderStatus.REFUNDED) &&
            (newStatus == OrderStatus.PENDING ||
                    newStatus == OrderStatus.PROCESSING)
        ) {
            throw IllegalStateException("Cannot revert order from final status ${this.status} to $newStatus")
        }

        if (this.status == OrderStatus.COMPLETED && newStatus == OrderStatus.REFUNDED) {
            // this is ok
        } else if (this.status != OrderStatus.COMPLETED && newStatus == OrderStatus.REFUNDED) {

            throw IllegalStateException("Cannot set status to REFUNDED unless order was COMPLETED. Current status: ${this.status}")
        }
        // TODO: recehck state transition validation if needed

        this.status = newStatus
    }

    fun applyOrderDiscount(calculatedAmount: Money, ruleId: Long?) {
        this.totalAmount.assertSameCurrency(calculatedAmount)
        require(calculatedAmount.isNonNegative()) { "Calculated order discount amount cannot be negative." }

        val currentTotalPreOrderDiscount = calculateTotalPreOrderDiscount()
        this.orderLevelDiscountAmount =
            if (calculatedAmount > currentTotalPreOrderDiscount) currentTotalPreOrderDiscount else calculatedAmount
        this.appliedOrderDiscountRuleId = ruleId

        if (calculatedAmount > currentTotalPreOrderDiscount) {
            log.debug { "Order ${this.id} discount capped at ${this.orderLevelDiscountAmount} (Original requested: $calculatedAmount)" }
        }

        recalculateTotals()
    }

    /** Removes any applied order-level discount. */
    fun removeOrderDiscount() {
        this.orderLevelDiscountAmount = this.orderLevelDiscountAmount.copy(amount = BigDecimal.ZERO)
        this.appliedOrderDiscountRuleId = null
        recalculateTotals()
    }

    /** Get total after item discounts, before order discount */
    fun calculateTotalPreOrderDiscount(): Money {
        if (orderItems.isEmpty()) return zero()
        val zero = zero()
        return orderItems.fold(zero) { sum, item -> sum + item.calculateFinalPrice() }
    }

    /** Helper to get a zero Money object in the order's currency */
    private fun zero(): Money {
        return this.finalAmount.copy(amount = BigDecimal.ZERO)
    }


}

private val log = KotlinLogging.logger {}