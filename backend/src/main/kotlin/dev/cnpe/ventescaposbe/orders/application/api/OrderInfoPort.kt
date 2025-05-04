package dev.cnpe.ventescaposbe.orders.application.api

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.application.api.dto.OrderStatsData


/**
 * Port defining operations for retrieving basic Order information needed by other modules.
 */
interface OrderInfoPort {

    /**
     * Retrieves specific data points from an order needed for external processing,
     * like updating customer statistics.
     *
     * @param orderId The ID of the order.
     * @return OrderStatsData containing the necessary info, or null if the order is not found.
     */
    fun getOrderStatsInfo(orderId: Long): OrderStatsData?


    /** Calculates total cash payments for completed orders in a session. */
    fun calculateSessionCashSales(sessionId: Long): Money

    /** Calculates total final amount for completed orders in a session. */
    fun calculateSessionTotalSales(sessionId: Long): Money

}