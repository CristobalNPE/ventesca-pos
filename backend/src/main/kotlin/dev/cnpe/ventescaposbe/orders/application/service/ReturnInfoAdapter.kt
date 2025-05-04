package dev.cnpe.ventescaposbe.orders.application.service

import dev.cnpe.ventescaposbe.business.application.api.BusinessDataPort
import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.application.api.ReturnInfoPort
import dev.cnpe.ventescaposbe.orders.domain.enums.RefundMethod
import dev.cnpe.ventescaposbe.orders.infrastructure.persistence.ReturnTransactionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class ReturnInfoAdapter(
    private val returnTransactionRepository: ReturnTransactionRepository,
    private val moneyFactory: MoneyFactory,
    private val businessDataPort: BusinessDataPort
) : ReturnInfoPort {

    override fun calculateSessionCashRefunds(sessionId: Long): Money {
        val currencyCode = businessDataPort.getBusinessPaymentData().currencyCode

        val totalCashRefundAmount = returnTransactionRepository.sumTotalRefundAmountBySessionIdAndMethod(
            sessionId,
            RefundMethod.CASH
        ) ?: BigDecimal.ZERO

        log.debug { "Port: Calculated cash refunds for session $sessionId: $totalCashRefundAmount $currencyCode" }
        return moneyFactory.createMoney(totalCashRefundAmount, currencyCode)
    }

    override fun calculateSessionTotalRefunds(sessionId: Long): Money {
        val currencyCode = businessDataPort.getBusinessPaymentData().currencyCode


        val totalRefundAmount = returnTransactionRepository.sumTotalRefundAmountBySessionId(sessionId)
            ?: BigDecimal.ZERO

        log.debug { "Port: Calculated total refunds for session $sessionId: $totalRefundAmount $currencyCode" }
        return moneyFactory.createMoney(totalRefundAmount, currencyCode)
    }
}