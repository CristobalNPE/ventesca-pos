package dev.cnpe.ventescaposbe.orders.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "Request payload for adding an item to an order.")
data class AddItemToOrderRequest(

    @field:NotNull
    @field:Positive
    @Schema(description = "ID of the product to add.", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    val productId: Long,

    @field:NotNull
    @field:Positive(message = "Quantity must be positive.")
    @Schema(
        description = "Quantity of the product to add.",
        example = "2.0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val quantity: Double
) {
}