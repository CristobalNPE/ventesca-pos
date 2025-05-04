package dev.cnpe.ventescaposbe.orders.domain.entity

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.domain.enums.RefundMethod
import dev.cnpe.ventescaposbe.orders.domain.enums.ReturnStatus
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "return_transactions")
class ReturnTransaction(

    @Column(name = "original_order_id", nullable = false, updatable = false)
    val originalOrderId: Long,

    @Column(name = "original_order_number", nullable = false, updatable = false)
    val originalOrderNumber: String,

    @Column(name = "branch_id", nullable = false, updatable = false)
    val branchId: Long,

    @Column(name = "user_idp_id", nullable = false, updatable = false)
    val userIdpId: String,

    @Column(name = "customer_id", updatable = false)
    val customerId: Long? = null,

    @Column(name = "return_timestamp", nullable = false, updatable = false)
    val returnTimestamp: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ReturnStatus = ReturnStatus.COMPLETED,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_refund_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "total_refund_currency", nullable = false, length = 3)
        )
    )
    var totalRefundAmount: Money,

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_method", nullable = false)
    var refundMethod: RefundMethod,

    @Column(name = "notes", length = 500)
    var notes: String? = null,

    @OneToMany(
        mappedBy = "returnTransaction",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val returnedItems: MutableList<ReturnedItem> = mutableListOf(),

    @Column(name = "session_id")
    var sessionId: Long? = null,

    // TODO: Should we add a return transaction number field later??

    id: Long? = null,
    version: Int = 0

) : BaseEntity(id, version) {

    /** Adds a returned item and recalculates the total refund amount. */
    fun addReturnedItem(item: ReturnedItem) {
        item.returnTransaction = this
        returnedItems.add(item)
        recalculateTotalRefundAmount()
    }

    /** Recalculates the total refund amount based on the current items. */
    fun recalculateTotalRefundAmount() {
        if (returnedItems.isEmpty()) {
            totalRefundAmount = totalRefundAmount.copy(amount = java.math.BigDecimal.ZERO)
            return
        }
        val zero = totalRefundAmount.copy(amount = java.math.BigDecimal.ZERO)
        totalRefundAmount = returnedItems.fold(zero) { sum, item ->
            sum + item.totalItemRefundAmount
        }
    }
}