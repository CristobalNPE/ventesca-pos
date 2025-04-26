package dev.cnpe.ventescabekotlin.inventory.application.api.dto

import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockUnitType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "InventoryItemInfo", description = "Information about inventory item stock and measurements.")
data class InventoryItemInfo(

    @Schema(description = "Current stock quantity of the item.", example = "10.0")
    val stockQuantity: Double?,

    @Schema(description = "Minimum required quantity for the item.", example = "5.0")
    val minimumQuantity: Double?,

    @Schema(description = "Unit of measure for the item.", example = "UNIT")
    val unitOfMeasure: StockUnitType
)