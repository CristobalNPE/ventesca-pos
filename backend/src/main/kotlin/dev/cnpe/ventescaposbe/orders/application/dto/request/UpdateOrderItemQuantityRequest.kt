package dev.cnpe.ventescaposbe.orders.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "Request payload for updating the quantity of an item in an order.")
data class UpdateOrderItemQuantityRequest(

    @field:Schema(
        description = "The new quantity for the order item. Must be greater than 0.",
        example = "3.0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotNull(message = "New quantity must be provided.")
    @field:Positive(message = "Quantity must be a positive number.")
    val newQuantity: Double
)