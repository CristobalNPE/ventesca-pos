package dev.cnpe.ventescaposbe.orders.application.api.dto

import dev.cnpe.ventescaposbe.currency.vo.Money
import java.time.OffsetDateTime

data class OrderStatsData(
    val orderId: Long,
    val customerId: Long?,
    val finalAmount: Money,
    val orderTimestamp: OffsetDateTime
)