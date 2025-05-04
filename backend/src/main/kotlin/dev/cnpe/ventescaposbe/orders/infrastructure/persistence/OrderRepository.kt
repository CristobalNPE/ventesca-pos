package dev.cnpe.ventescaposbe.orders.infrastructure.persistence

import dev.cnpe.ventescaposbe.business.domain.enums.PaymentMethod
import dev.cnpe.ventescaposbe.orders.domain.entity.Order
import dev.cnpe.ventescaposbe.orders.domain.enums.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface OrderRepository : JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    fun findByOrderNumber(orderNumber: String): Order?

    @Query(
        """
            select o
            from Order o
            left join fetch o.orderItems
            where o.id = :orderId
        """
    )
    fun findByIdWithItems(orderId: Long): Order?

    @Query(
        """
            select o
            from Order o
            left join fetch o.orderItems
            left join fetch o.payments
            where o.id = :orderId
        """
    )
    fun findByIdWithItemsAndPayments(orderId: Long): Order?


    @Query(
        """
            select sum(p.amount.amount)
            from Order o join o.payments p
            where o.sessionId = :sessionId
            and o.status = :orderStatus
            and p.paymentMethod = :paymentMethod
            and p.status = 'COMPLETED'
        """
    )
    fun sumCompletedCashPaymentsBySessionId(
        @Param("sessionId") sessionId: Long,
        @Param("paymentMethod") paymentMethod: PaymentMethod,
        @Param("orderStatus") orderStatus: OrderStatus = OrderStatus.COMPLETED
    ): BigDecimal?


    @Query(
        """
            select sum(o.finalAmount.amount)
            from Order o
            where o.sessionId = :sessionId
            and o.status = :orderStatus
        """
    )
    fun sumFinalAmountBySessionIdAndStatus(
        @Param("sessionId") sessionId: Long,
        @Param("orderStatus") orderStatus: OrderStatus = OrderStatus.COMPLETED
    ): BigDecimal?

    @Query(value = "SELECT NEXTVAL('order_number_seq')", nativeQuery = true)
    fun getNextOrderNumberSequenceValue(): Long
}