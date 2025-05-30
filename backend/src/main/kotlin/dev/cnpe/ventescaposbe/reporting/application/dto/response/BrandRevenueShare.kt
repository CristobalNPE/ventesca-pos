package dev.cnpe.ventescaposbe.reporting.application.dto.response

import dev.cnpe.ventescaposbe.currency.vo.Money
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Revenue contribution of a single brand.")
data class BrandRevenueShare(

    @Schema(description = "ID of the brand.")
    val brandId: Long,

    @Schema(description = "Name of the brand.")
    val brandName: String,

    @Schema(description = "Total revenue generated by this brand.")
    val totalRevenue: Money,

    @Schema(description = "Percentage of total revenue contributed by this category.", example = "15.0")
    val percentageOfTotal: BigDecimal
)