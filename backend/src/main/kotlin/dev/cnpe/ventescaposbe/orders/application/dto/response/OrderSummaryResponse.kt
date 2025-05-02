package dev.cnpe.ventescaposbe.orders.application.dto.response

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.domain.enums.OrderStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Summary view of an order, suitable for lists.")
data class OrderSummaryResponse(
    @Schema(description = "Unique ID of the order.")
    val id: Long,

    @Schema(description = "System-generated order number.")
    val orderNumber: String,

    @Schema(description = "Current status of the order.")
    val status: OrderStatus,

    @Schema(description = "ID of the branch where the order was placed.")
    val branchId: Long,

    @Schema(description = "ID of the user who processed the order.")
    val userIdpId: String, // TODO: maybe fetch user display name if needed?

    @Schema(description = "Timestamp when the order was created/placed.")
    val orderTimestamp: OffsetDateTime,

    @Schema(description = "The final amount due or paid.")
    val finalAmount: Money,

    @Schema(description = "Number of items in the order.")
    val itemCount: Int
)