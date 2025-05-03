package dev.cnpe.ventescaposbe.catalog.api.dto

import dev.cnpe.ventescaposbe.catalog.domain.enums.ProductStatus
import dev.cnpe.ventescaposbe.currency.vo.Money
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Product sale information.")
data class ProductSaleInfo(

    @Schema(description = "Unique identifier of the product")
    val productId: Long,

    @Schema(description = "Name of the product")
    val productName: String,

    @Schema(description = "Stock keeping unit (SKU) of the product")
    val productSku: String?,

    @Schema(description = "ID of the category this product belongs to")
    val categoryId: Long?,

    @Schema(description = "ID of the brand this product belongs to")
    val brandId: Long?,

    @Schema(description = "ID of the supplier this product is sourced from")
    val supplierId: Long?,

    @Schema(description = "Current status of the product")
    val status: ProductStatus,

    @Schema(description = "Current selling price of the product")
    val currentSellingPrice: Money?,
)
