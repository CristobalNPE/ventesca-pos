package dev.cnpe.ventescaposbe.orders.application.service

import dev.cnpe.ventescaposbe.business.application.api.BusinessDataPort
import dev.cnpe.ventescaposbe.business.domain.enums.PaymentMethod
import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.application.api.OrderInfoPort
import dev.cnpe.ventescaposbe.orders.application.api.dto.OrderStatsData
import dev.cnpe.ventescaposbe.orders.domain.enums.OrderStatus
import dev.cnpe.ventescaposbe.orders.infrastructure.persistence.OrderRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class OrderInfoAdapter(
    private val orderRepository: OrderRepository,
    private val businessDataPort: BusinessDataPort,
    private val moneyFactory: MoneyFactory
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

    override fun calculateSessionCashSales(sessionId: Long): Money {
        val currencyCode = businessDataPort.getBusinessPaymentData().currencyCode


        val totalCashPaymentAmount = orderRepository.sumCompletedCashPaymentsBySessionId(
            sessionId,
            PaymentMethod.CASH,
            OrderStatus.COMPLETED
        ) ?: BigDecimal.ZERO

        log.debug { "Port: Calculated cash sales for session $sessionId: $totalCashPaymentAmount $currencyCode" }
        return moneyFactory.createMoney(totalCashPaymentAmount, currencyCode)
    }

    override fun calculateSessionTotalSales(sessionId: Long): Money {
        val currencyCode = businessDataPort.getBusinessPaymentData().currencyCode


        val totalSalesAmount = orderRepository.sumFinalAmountBySessionIdAndStatus(
            sessionId,
            OrderStatus.COMPLETED
        ) ?: BigDecimal.ZERO

        log.debug { "Port: Calculated total sales for session $sessionId: $totalSalesAmount $currencyCode" }
        return moneyFactory.createMoney(totalSalesAmount, currencyCode)
    }
}