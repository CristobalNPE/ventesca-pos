package dev.cnpe.ventescaposbe.orders.domain.entity

import dev.cnpe.ventescaposbe.business.domain.enums.PaymentMethod
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.domain.enums.PaymentStatus
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "payments")
class Payment(


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    val paymentMethod: PaymentMethod,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "amount", nullable = false)),
        AttributeOverride(name = "currencyCode", column = Column(name = "currency_code", nullable = false, length = 3))
    )
    val amount: Money,

    @Column(name = "payment_timestamp", nullable = false)
    val paymentTimestamp: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(name = "transaction_reference")
    var transactionReference: String? = null,


    id: Long? = null,
    version: Int = 0
): BaseEntity(id,version) {

    //TODO : payment specific logic? refund handling?
}