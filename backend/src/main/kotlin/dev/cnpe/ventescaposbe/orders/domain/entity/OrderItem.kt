package dev.cnpe.ventescaposbe.orders.domain.entity

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.math.RoundingMode

@Entity
@Table(name = "order_items")
class OrderItem(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,

    @Column(name = "product_id", nullable = false, updatable = false)
    val productId: Long,

    @Column(name = "product_name_snapshot", nullable = false)
    var productNameSnapshot: String,

    @Column(name = "sku_snapshot", nullable = true)
    var skuSnapshot: String?,

    @Column(name = "quantity", nullable = false)
    var quantity: Double,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "unit_price_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "unit_price_currency", nullable = false, length = 3)
        )
    )
    var unitPrice: Money,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "net_unit_price_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "net_unit_price_currency", nullable = false, length = 3)
        )
    )
    var netUnitPrice: Money,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "discount_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "discount_currency", nullable = false, length = 3)
        )
    )
    var discountAmount: Money,

    id: Long? = null,
    version: Int = 0
) : BaseEntity(id, version) {

    init {
        require(quantity > 0) { "OrderItem quantity must be positive" }
        unitPrice.assertSameCurrency(netUnitPrice)
        unitPrice.assertSameCurrency(discountAmount)
        require(unitPrice.isPositive()) { "Unit price must be positive" }
        require(netUnitPrice.isPositive()) { "Net unit price must be positive" }
        require(discountAmount.isNonNegative()) { "Discount amount cannot be negative" }
    }

    /** Calculates the total gross price for this line item (unit price * quantity). */
    fun calculateTotalPrice(): Money {
        return unitPrice.times(BigDecimal.valueOf(quantity))
    }

    /** Calculates the total net price for this line item (net unit price * quantity). */
    fun calculateTotalNetPrice(): Money {
        return netUnitPrice.times(BigDecimal.valueOf(quantity))
    }

    /** Calculates the final price for this line item after discount. */
    fun calculateFinalPrice(): Money {
        val total = calculateTotalPrice() - discountAmount
        return if (total.isNegative()) total.copy(amount = BigDecimal.ZERO) else total
    }

    /** Calculates the final net price for this line item after proportionally applying the discount. */
    fun calculateFinalNetPrice(): Money {
        val grossTotal = calculateTotalPrice()
        val netTotal = calculateTotalNetPrice()
        val finalGross = calculateFinalPrice()

        if (grossTotal.isZero()) return netTotal.copy(amount = BigDecimal.ZERO)

        val finalProportion = finalGross.amount.divide(grossTotal.amount, 10, RoundingMode.HALF_UP)

        val finalNetAmount = netTotal.amount.multiply(finalProportion)

        return netTotal.copy(amount = finalNetAmount.setScale(netTotal.amount.scale(), RoundingMode.HALF_UP))
    }
}