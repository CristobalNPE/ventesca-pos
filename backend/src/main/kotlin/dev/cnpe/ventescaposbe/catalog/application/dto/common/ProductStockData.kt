package dev.cnpe.ventescaposbe.catalog.application.dto.common

import dev.cnpe.ventescaposbe.inventory.domain.enums.StockUnitType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Stock information summary (provided by Inventory module).")
data class ProductStockData(
    @Schema(description = "Current total quantity across all locations.")
    val currentQuantity: Double?,

    @Schema(description = "Configured minimum quantity level.", nullable = true)
    val minimumQuantity: Double?,

    @Schema(description = "Unit of measure (e.g., UNIT, KG).")
    val unitOfMeasure: StockUnitType
)