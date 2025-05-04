package dev.cnpe.ventescaposbe.orders.application.api

import dev.cnpe.ventescaposbe.currency.vo.Money

/** Port for retrieving return transaction info needed by other modules. */
interface ReturnInfoPort {

    /** Calculates total cash refunds for completed return transactions in a session. */
    fun calculateSessionCashRefunds(sessionId: Long): Money

    /** Calculates total refund amount for completed return transactions in a session. */
    fun calculateSessionTotalRefunds(sessionId: Long): Money
}