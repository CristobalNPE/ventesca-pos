package dev.cnpe.ventescaposbe.promotions.domain.model

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountApplicability
import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountRuleType
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(
    name = "discount_rules",
    indexes = [
        Index(name = "idx_discount_rules_active_dates", columnList = "is_active, start_date, end_date"),
        Index(name = "idx_discount_rules_applicability", columnList = "applicability")
    ]
)
class DiscountRule(

    @Column(name = "name", nullable = false, unique = true)
    var name: String,

    @Column(name = "description")
    var description: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: DiscountRuleType,

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    var value: BigDecimal,

    @Column(name = "start_date")
    var startDate: OffsetDateTime? = null,

    @Column(name = "end_date")
    var endDate: OffsetDateTime? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "applicability", nullable = false)
    var applicability: DiscountApplicability = DiscountApplicability.ORDER_TOTAL,


    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "discount_rule_target_products",
        joinColumns = [JoinColumn(name = "discount_rule_id")],
    )
    @Column(name = "product_id")
    var targetProductIds: MutableSet<Long> = mutableSetOf(),

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "discount_rule_target_categories",
        joinColumns = [JoinColumn(name = "discount_rule_id")],
    )
    @Column(name = "category_id")
    var targetCategoryIds: MutableSet<Long> = mutableSetOf(),

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "discount_rule_target_brands",
        joinColumns = [JoinColumn(name = "discount_rule_id")],
    )
    @Column(name = "brand_id")
    var targetBrandIds: MutableSet<Long> = mutableSetOf(),

    @Column(name = "minimum_quantity")
    var minimumQuantity: Int? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "minimum_spend_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "minimum_spend_currency", length = 3))
    )
    var minimumSpend: Money? = null,


    @Column(name = "is_combinable", nullable = false)
    var isCombinable: Boolean = false,

    id: Long? = null,
    version: Int = 0
) : BaseEntity(id, version) {

    /** Checks if the rule is currently valid based on dates and active status. */
    fun isValidNow(): Boolean {
        if (!isActive) return false
        val now = OffsetDateTime.now()
        val afterStart = startDate == null || !now.isBefore(startDate)
        val beforeEnd = endDate == null || now.isBefore(endDate)
        return afterStart && beforeEnd
    }

    @PrePersist
    @PreUpdate
    fun validateState() {
        when (type) {
            DiscountRuleType.ITEM_PERCENTAGE, DiscountRuleType.ORDER_PERCENTAGE ->
                require(value >= BigDecimal.ZERO && value <= BigDecimal(100)) {
                    "Percentage value must be between 0 and 100"
                }

            DiscountRuleType.ITEM_FIXED_AMOUNT, DiscountRuleType.ORDER_FIXED_AMOUNT ->
                require(value >= BigDecimal.ZERO) {
                    "Fixed amount value cannot be negative"
                }
        }
    }
}