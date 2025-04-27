package dev.cnpe.ventescabekotlin.inventory.application.api.dto

import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockUnitType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Aggregated inventory summary for a product across all branches.")
data class ProductInventorySummary(

    @Schema(description = "Total current stock quantity across all locations.", example = "25.0")
    val totalStockQuantity: Double,

    @Schema(
        description = "Representative minimum required quantity. Aggregation rule depends on implementation (e.g., first branch's value, average, null if inconsistent). Check service implementation details.",
        example = "10.0",
        nullable = true // nullable as aggregation rule is unclear
    )
    val representativeMinimumQuantity: Double?,

    @Schema(
        description = "Unit of measure for the item (assumed consistent across branches).",
        example = "UNIT"
    )
    val unitOfMeasure: StockUnitType
)