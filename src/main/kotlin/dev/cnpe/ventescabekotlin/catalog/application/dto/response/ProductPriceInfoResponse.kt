package dev.cnpe.ventescabekotlin.catalog.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

@Schema(description = "Information about a product price.")
data class ProductPriceInfoResponse(

    @Schema(description = "Selling price of the product.")
    val sellingPrice: BigDecimal,

    @Schema(description = "Purchase price of the product.")
    val supplierCost: BigDecimal,

    @Schema(description = "Profit of the product.")
    val profit: BigDecimal,

    @Schema(description = "Profit margin of the product.")
    val profitMargin: BigDecimal?,

    @Schema(description = "Start date of the product price.")
    val startDate: LocalDateTime,

    @Schema(description = "End date of the product price.")
    val endDate: LocalDateTime?,

    @Schema(description = "Reason for the price change.")
    val changeReason: String

)
