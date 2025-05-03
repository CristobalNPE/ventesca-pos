package dev.cnpe.ventescaposbe.inventory.application.dto.request

import dev.cnpe.ventescaposbe.inventory.domain.enums.StockModificationReason
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

@Schema(description = "Request to manually adjust stock for a product in a specific branch.")
data class AdjustStockRequest(

    @field:NotNull(message = "Branch ID must be provided.")
    @field:Positive(message = "Branch ID must be positive.")
    @Schema(description = "ID of the branch where stock is being adjusted.", example = "101", required = true)
    val branchId: Long,

    @field:NotNull(message = "Adjustment amount must be provided.")
    @Schema(
        description = "The amount to adjust the stock by (positive for increase, negative for decrease). Cannot be zero.",
        example = "-2.0",
        required = true
    )
    val adjustmentAmount: Double,

    @field:NotNull(message = "Reason for adjustment must be provided.")
    @Schema(description = "The reason for this stock adjustment.", example = "DAMAGED", required = true)
    val reason: StockModificationReason,

    @field:Size(max = 200, message = "Notes cannot exceed 200 characters.")
    @Schema(description = "Optional notes detailing the adjustment.", example = "Dropped during handling.")
    val notes: String? = null
) {
    init {
        require(adjustmentAmount != 0.0) { "Adjustment amount cannot be zero." }
        val forbiddenReasons = setOf(StockModificationReason.SALE, StockModificationReason.RETURN)
        require(!forbiddenReasons.contains(reason)) {
            "Manual adjustments cannot use SALE or RETURN reasons. Use order/return endpoints."
        }
    }
}