package dev.cnpe.ventescaposbe.orders.application.service

import dev.cnpe.ventescaposbe.orders.application.api.OrderInfoPort
import dev.cnpe.ventescaposbe.orders.application.api.dto.OrderStatsData
import dev.cnpe.ventescaposbe.orders.infrastructure.persistence.OrderRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class OrderInfoAdapter(
    private val orderRepository: OrderRepository
) : OrderInfoPort {

    override fun getOrderStatsInfo(orderId: Long): OrderStatsData? {
        log.debug { "Port: Getting order stats info for Order ID: $orderId" }
        return orderRepository.findById(orderId).map { order ->
            OrderStatsData(
                orderId = order.id!!,
                customerId = order.customerId,
                finalAmount = order.finalAmount,
                orderTimestamp = order.orderTimestamp
            )
        }.orElse(null)
    }
}