package dev.cnpe.ventescaposbe.sessions.domain.entity

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.sessions.domain.enums.CashMovementReason
import dev.cnpe.ventescaposbe.sessions.domain.enums.CashMovementType
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "cash_movements")
class CashMovement(

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    var session: RegisterSession,

    @Column(name = "movement_timestamp", nullable = false, updatable = false)
    val timestamp: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    val type: CashMovementType,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "amount", nullable = false)),
        AttributeOverride(name = "currencyCode", column = Column(name = "currency", nullable = false, length = 3))
    )
    val amount: Money,

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, updatable = false)
    val reason: CashMovementReason,

    @Column(name = "notes", length = 255)
    var notes: String? = null,

    @Column(name = "user_idp_id", nullable = false, updatable = false)
    val userIdpId: String,

    id: Long? = null,
    version: Int = 0

) : BaseEntity(id, version) {
    init {
        require(amount.isPositive()) { "Cash movement amount must be positive." }
    }
}