package dev.cnpe.ventescabekotlin.catalog.application.dto.request

import dev.cnpe.ventescabekotlin.catalog.domain.enums.PriceChangeReason
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Schema(description = "Request to update the selling price of a product.")
data class UpdateProductSellingPriceRequest(

    @field:NotNull
    @field:DecimalMin(value = "0.01", message = "Selling price must be positive")
    @Schema(description = "The new selling price amount.", example = "1349.99")
    val sellingPrice: BigDecimal,

    @field:NotNull
    @Schema(description = "Indicates if the provided sellingPrice includes tax.", example = "false")
    val taxInclusive: Boolean,

    @field:NotNull
    @Schema(description = "Reason for this price change.", example = "COST_CHANGE")
    val reason: PriceChangeReason
)