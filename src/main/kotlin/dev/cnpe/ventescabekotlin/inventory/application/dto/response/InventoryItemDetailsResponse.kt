package dev.cnpe.ventescabekotlin.inventory.application.dto.response

import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockUnitType
import io.swagger.v3.oas.annotations.media.Schema

// FIXME: This DTO seems ambiguous as per the original Java comment.
// Does it represent ONE item in ONE branch, or aggregated data?
// Assuming it's for ONE branch for now. Needs clarification.
@Schema(description = "Detailed inventory status for a specific product in a specific branch.")
data class InventoryItemDetailsResponse(

    @Schema(description = "Current quantity in this specific branch.", example = "12.0")
    val currentQuantity: Double,

    @Schema(description = "Minimum quantity configured for this branch.", example = "5.0")
    val minQuantity: Double,

    @Schema(description = "Unit of measure.", example = "UNIT")
    val unitOfMeasure: StockUnitType,

    @Schema(description = "Indicates if stock is low (at or below minimum) in this branch.")
    val isLowStock: Boolean,

    @Schema(description = "Indicates if stock is zero in this branch.")
    val isOutOfStock: Boolean,

    @Schema(description = "Quantity available above the minimum level in this branch.", example = "7.0")
    val availableQuantity: Double,

    @Schema(description = "Formatted quantity string (e.g., '12 units', '1.50 kg').", example = "12 units")
    val formattedQuantity: String,

    @Schema(description = "Indicates if the stock level suggests a restock is needed for this branch.")
    val needsRestock: Boolean
)