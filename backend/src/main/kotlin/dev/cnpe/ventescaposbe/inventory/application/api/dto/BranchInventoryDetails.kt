package dev.cnpe.ventescaposbe.inventory.application.api.dto

import dev.cnpe.ventescaposbe.inventory.domain.enums.StockUnitType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Detailed inventory status for a specific product in a specific branch.")
data class BranchInventoryDetails(

    @Schema(description = "Product ID", example = "123")
    val productId: Long,

    @Schema(description = "Branch ID", example = "101")
    val branchId: Long,

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

    @Schema(description = "Indicates if the stock level suggests a restock is needed for this branch.")
    val needsRestock: Boolean
)
