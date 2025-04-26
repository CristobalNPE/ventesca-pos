package dev.cnpe.ventescabekotlin.catalog.application.dto.common

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Calculated pricing information for the product at a point in time.")
data class ProductPricingData(

    @Schema(description = "Currency code.", example = "CLP")
    val currency: String,

    @Schema(description = "Current net selling price (excluding tax).")
    val currentNetSellingPrice: BigDecimal,

    @Schema(description = "Current gross selling price (including tax).")
    val currentSellingPrice: BigDecimal,

    @Schema(description = "Current supplier cost.")
    val currentSupplierCost: BigDecimal,

    @Schema(description = "Current profit amount (Gross Selling Price - Cost).")
    val currentProfit: BigDecimal,

    @Schema(description = "Current profit margin percentage ((Profit / Cost) * 100).")
    val currentProfitMargin: BigDecimal?
)