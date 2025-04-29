package dev.cnpe.ventescaposbe.orders.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents the lifecycle status of an order.")
enum class OrderStatus : DomainEnum {

    @Schema(description = "Order initiated, items can be added/removed.")
    PENDING,

    @Schema(description = "Payment process initiated or partially completed.")
    PROCESSING,

    @Schema(description = "Order fully paid and finalized.")
    COMPLETED,

    @Schema(description = "Order cancelled before completion.")
    CANCELLED,

    @Schema(description = "Order was completed but subsequently refunded (partially or fully).")
    REFUNDED
}