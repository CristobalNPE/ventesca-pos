package dev.cnpe.ventescaposbe.orders.application.dto.request

import dev.cnpe.ventescaposbe.orders.domain.enums.ReturnReason
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "Details for a single item being returned.")
data class ReturnItemDetail(

    @field:NotNull(message = "Original Order Item ID must be provided.")
    @field:Positive(message = "Original Order Item ID must be positive.")
    @Schema(description = "ID of the specific line item in the original order.", example = "54321")
    val originalOrderItemId: Long,

    @field:NotNull(message = "Quantity to return must be provided.")
    @field:Positive(message = "Quantity to return must be positive.")
    @Schema(description = "The quantity of this specific item being returned.", example = "1.0")
    val quantityToReturn: Double,

    @field:NotNull(message = "Return reason must be provided.")
    @Schema(description = "Reason for returning this item.", example = "DEFECTIVE")
    val reason: ReturnReason,

    @field:NotNull(message = "Restock decision must be provided.")
    @Schema(description = "Whether this returned item should be put back into sellable stock.", example = "false")
    val restock: Boolean
)