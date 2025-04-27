package dev.cnpe.ventescabekotlin.inventory.application.dto.request

import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockModificationReason
import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockUnitType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "Request to update stock details for a specific product in a specific branch.")
data class UpdateStockRequest(

    @field:NotNull(message = "Stock quantity must be provided.")
    @field:Min(value = 0, message = "Stock quantity cannot be negative.")
    @Schema(
        description = "The new absolute stock quantity for the item in this branch.",
        example = "15.0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val stockQuantity: Double,

    @field:NotNull(message = "Minimum stock level must be provided.")
    @field:Min(value = 0, message = "Minimum stock level cannot be negative.")
    @Schema(
        description = "The new minimum stock level for the item in this branch.",
        example = "5.0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val minimumStockLevel: Double,

    @field:NotNull(message = "Unit of measure must be provided.")
    @Schema(
        description = "The unit of measure for the stock.",
        example = "UNIT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val unitOfMeasure: StockUnitType,

    @field:NotNull(message = "Branch ID must be provided.")
    @field:Positive(message = "Branch ID must be a positive number.")
    @Schema(
        description = "The ID of the branch where the stock is being updated.",
        example = "101",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val branchId: Long,

    @Schema(description = "Reason for the stock modification (required if quantity changes).", example = "RESTOCK")
    val reason: StockModificationReason?
)