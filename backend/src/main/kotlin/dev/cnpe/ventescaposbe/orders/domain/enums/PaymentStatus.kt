package dev.cnpe.ventescaposbe.orders.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents the status of an individual payment attempt within an order.")
enum class PaymentStatus : DomainEnum {

    @Schema(description = "Payment initiated but not yet confirmed.")
    PENDING,

    @Schema(description = "Payment successfully processed.")
    COMPLETED,

    @Schema(description = "Payment attempt failed.")
    FAILED,

    @Schema(description = "Payment was completed but subsequently refunded.")
    REFUNDED
}