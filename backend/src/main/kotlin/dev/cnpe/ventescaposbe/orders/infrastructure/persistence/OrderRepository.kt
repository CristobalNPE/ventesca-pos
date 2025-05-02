package dev.cnpe.ventescaposbe.orders.infrastructure.persistence

import dev.cnpe.ventescaposbe.orders.domain.entity.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderRepository : JpaRepository<Order, Long> {

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


    @Query(value = "SELECT NEXTVAL('order_number_seq')", nativeQuery = true)
    fun getNextOrderNumberSequenceValue(): Long
}