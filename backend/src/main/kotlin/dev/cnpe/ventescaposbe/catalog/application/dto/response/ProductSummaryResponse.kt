package dev.cnpe.ventescaposbe.catalog.application.dto.response

import dev.cnpe.ventescaposbe.catalog.domain.enums.ProductStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(name = "ProductSimpleResponse", description = "Basic summary information for a product, suitable for lists.")
data class ProductSummaryResponse(

    @Schema(description = "Product ID.", requiredMode = Schema.RequiredMode.REQUIRED)
    val id: Long,

    @Schema(description = "Product name.", requiredMode = Schema.RequiredMode.REQUIRED, example = "Churu 30g")
    val name: String,

    @Schema(description = "Product barcode.", requiredMode = Schema.RequiredMode.REQUIRED, example = "14658974987")
    val barcode: String,

    @Schema(description = "Current selling price (tax included).", requiredMode = Schema.RequiredMode.REQUIRED)
    val currentSellingPrice: BigDecimal,

    @Schema(description = "Total stock across all locations.", requiredMode = Schema.RequiredMode.REQUIRED)
    val currentTotalStock: Double,

    @Schema(description = "Current status.", requiredMode = Schema.RequiredMode.REQUIRED)
    val status: ProductStatus,

    @Schema(description = "Associated category ID.", requiredMode = Schema.RequiredMode.REQUIRED)
    val categoryId: Long?

)
