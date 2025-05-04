package dev.cnpe.ventescaposbe.sessions.domain.entity

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.sessions.domain.enums.SessionStatus
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "register_sessions", indexes = [
        Index(name = "idx_session_user_branch_status", columnList = "user_idp_id, branch_id, status"),
        Index(name = "idx_session_opening_time", columnList = "opening_time"),
        Index(name = "idx_session_branch_id", columnList = "branch_id")
    ]
)
class RegisterSession(

    @Column(name = "session_number", nullable = false, unique = true)
    var sessionNumber: String,

    @Column(name = "branch_id", nullable = false, updatable = false)
    val branchId: Long,

    @Column(name = "user_idp_id", nullable = false, updatable = false)
    val userIdpId: String,

    @Column(name = "register_id", updatable = false)
    val registerId: String? = null, // id of the register device?

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: SessionStatus = SessionStatus.OPEN,

    @Column(name = "opening_time", nullable = false, updatable = false)
    val openingTime: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "closing_time")
    var closingTime: OffsetDateTime? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "opening_float_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "opening_float_currency", nullable = false, length = 3)
        )
    )
    val openingFloat: Money, // starting cash

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "closing_counted_cash_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "closing_counted_cash_currency", length = 3))
    )
    var closingCountedCash: Money? = null, // cash at end


    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "calc_cash_sales_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "calc_cash_sales_currency", length = 3))
    )
    var calculatedCashSales: Money? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "calc_cash_refunds_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "calc_cash_refunds_currency", length = 3))
    )
    var calculatedCashRefunds: Money? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "calc_pay_ins_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "calc_pay_ins_currency", length = 3))
    )
    var calculatedPayIns: Money? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "calc_pay_outs_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "calc_pay_outs_currency", length = 3))
    )
    var calculatedPayOuts: Money? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "calc_expected_cash_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "calc_expected_cash_currency", length = 3))
    )
    var calculatedExpectedCash: Money? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "cash_variance_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "cash_variance_currency", length = 3))
    )
    var cashVariance: Money? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_sales_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "total_sales_currency", length = 3))
    )
    var totalSalesAmount: Money? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_refund_amount")),
        AttributeOverride(name = "currencyCode", column = Column(name = "total_refund_currency", length = 3))
    )
    var totalRefundAmount: Money? = null,

    @Column(name = "notes", length = 500)
    var notes: String? = null,

    @OneToMany(mappedBy = "session", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val cashMovements: MutableList<CashMovement> = mutableListOf(),

    id: Long? = null,
    version: Int = 0

) : BaseEntity(id, version) {

    fun closeSession(
        countedCash: Money,
        cashSales: Money,
        cashRefunds: Money,
        payIns: Money,
        payOuts: Money,
        totalSales: Money,
        totalRefunds: Money,
        closingNotes: String?
    ) {
        require(status == SessionStatus.OPEN) { "Cannot close a session that is not OPEN." }
        openingFloat.assertSameCurrency(countedCash)
        openingFloat.assertSameCurrency(cashSales)
        // .TODO: assertions for all Money obj

        this.closingCountedCash = countedCash
        this.calculatedCashSales = cashSales
        this.calculatedCashRefunds = cashRefunds
        this.calculatedPayIns = payIns
        this.calculatedPayOuts = payOuts
        this.totalSalesAmount = totalSales
        this.totalRefundAmount = totalRefunds

        this.calculatedExpectedCash = openingFloat + cashSales - cashRefunds + payIns - payOuts
        this.cashVariance = countedCash - this.calculatedExpectedCash!!

        this.status = SessionStatus.CLOSED
        this.closingTime = OffsetDateTime.now()
        this.notes = closingNotes ?: this.notes
    }

    fun addCashMovement(movement: CashMovement) {
        movement.session = this
        this.cashMovements.add(movement)
    }
}