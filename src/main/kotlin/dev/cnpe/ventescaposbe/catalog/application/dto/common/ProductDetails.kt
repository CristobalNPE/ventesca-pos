package dev.cnpe.ventescaposbe.catalog.application.dto.common

import dev.cnpe.ventescaposbe.catalog.domain.enums.ProductStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Core details of the product.")
data class ProductDetails(

    @Schema(description = "Display name of the product.", example = "iPhone 14")
    val name: String,
    @Schema(description = "Stock Keeping Unit (SKU). Unique identifier.", example = "LP-BLK-16-512")
    val sku: String?,
    @Schema(description = "Barcode (EAN, UPC, etc.).", example = "1234567890123")
    val barcode: String,
    @Schema(description = "Product description.")
    val description: String?,
    @Schema(description = "Current status of the product.")
    val status: ProductStatus

)
