package dev.cnpe.ventescabekotlin.catalog.application.dto.common

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Stock information summary (provided by Inventory module).")
data class ProductStockData(
    @Schema(description = "Current total quantity across all locations.")
    val currentQuantity: Double?,

    @Schema(description = "Configured minimum quantity level.")
    val minimumQuantity: Double?,

    @Schema(description = "Unit of measure (e.g., UNIT, KG).")
    val unitOfMeasure: String? // FIXME: Enum from Inventory?
)