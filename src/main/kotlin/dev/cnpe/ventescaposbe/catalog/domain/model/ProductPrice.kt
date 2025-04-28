package dev.cnpe.ventescaposbe.catalog.domain.model

import dev.cnpe.ventescaposbe.catalog.domain.enums.PriceChangeReason
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Entity
@Table(name = "product_prices")
class ProductPrice(

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "selling_price_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "selling_price_currency", nullable = false, length = 3)
        )
    )
    val sellingPrice: Money,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "cost_price_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "cost_price_currency", nullable = false, length = 3)
        )
    )
    val supplierCost: Money,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,


    @Column(name = "start_date", nullable = false)
    val startDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "end_date")
    var endDate: LocalDateTime? = null,

    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    val reason: PriceChangeReason,

    id: Long? = null,
    version: Int = 0,
) : BaseEntity(id, version) {

    fun calculateProfit(): Money {
        sellingPrice.assertSameCurrency(supplierCost)
        return sellingPrice - supplierCost
    }

    fun calculateProfitMargin(): BigDecimal {
        sellingPrice.assertSameCurrency(supplierCost)
        if (supplierCost.isZero()) {
            return BigDecimal.ZERO
        }
        val profitAmount = calculateProfit().amount
        return profitAmount.divide(supplierCost.amount, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
    }

    fun isActive(): Boolean {
        val now = LocalDateTime.now()
        return !startDate.isAfter(now) && (endDate == null || endDate!!.isAfter(now))
    }

    fun validatePrices() {
        require(sellingPrice.isNonNegative()) { "Selling price must be non-negative" }
        require(supplierCost.isNonNegative()) { "Supplier cost must be non-negative" }
        sellingPrice.assertSameCurrency(supplierCost)
    }

    @PrePersist
    fun prePersist() {
        validatePrices()
    }

    @PreUpdate
    fun preUpdate() {
        validatePrices()
    }
}