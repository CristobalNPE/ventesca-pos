package dev.cnpe.ventescaposbe.orders.domain.entity

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.domain.enums.ReturnReason
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "returned_items")
class ReturnedItem(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_transaction_id", nullable = false)
    var returnTransaction: ReturnTransaction,

    @Column(name = "original_order_item_id", nullable = false, updatable = false)
    val originalOrderItemId: Long,

    @Column(name = "product_id", nullable = false, updatable = false)
    val productId: Long,

    @Column(name = "product_name_snapshot", nullable = false, updatable = false)
    val productNameSnapshot: String,

    @Column(name = "sku_snapshot", updatable = false)
    val skuSnapshot: String?,

    @Column(name = "quantity_returned", nullable = false, updatable = false)
    val quantityReturned: Double,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "unit_refund_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "unit_refund_currency", nullable = false, length = 3)
        )
    )
    val unitRefundAmount: Money,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_item_refund_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "total_item_refund_currency", nullable = false, length = 3)
        )
    )
    var totalItemRefundAmount: Money,

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, updatable = false)
    val reason: ReturnReason,

    @Column(name = "restock", nullable = false, updatable = false)
    val restock: Boolean,

    id: Long? = null,
    version: Int = 0

) : BaseEntity(id, version) {
    init {

        require(quantityReturned > 0) { "Quantity returned must be positive." }
        require(unitRefundAmount.isPositive()) { "Unit refund amount must be positive." }
        require(totalItemRefundAmount.isPositive()) { "Total item refund amount must be positive." }

        val calculatedTotal = unitRefundAmount.times(BigDecimal.valueOf(quantityReturned))

        require(totalItemRefundAmount.amount.compareTo(calculatedTotal.amount) == 0) {
            "Total item refund amount ($totalItemRefundAmount) does not match calculation ($calculatedTotal)"
        }
    }
}