package dev.cnpe.ventescabekotlin.catalog.application.dto.request

import dev.cnpe.ventescabekotlin.catalog.domain.enums.ProductStatus
import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockUnitType
import dev.cnpe.ventescabekotlin.shared.domain.vo.Image
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length
import java.math.BigDecimal

// TODO: Add validation schemas for inventory module types when available

@Schema(description = "Request to create a full product with all necessary details.")
data class CreateProductRequest(

    @field:NotBlank @field:Length(min = 3, max = 70)
    @Schema(description = "Display name of the product.", example = "Laptop Pro 16GB")
    val name: String,

    @field:NotBlank @field:Length(min = 3, max = 70)
    @Schema(description = "Barcode (EAN, UPC, etc.). Must be unique.", example = "9780123456786")
    val barcode: String,

    @field:Length(min = 3, max = 1000)
    @Schema(description = "Detailed description of the product.", example = "High-performance laptop...")
    val description: String?,

    @field:NotNull @field:DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Selling price (excluding tax).", example = "1299.99")
    val sellingPrice: BigDecimal,

    @field:NotNull @field:DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Supplier cost (unit cost).", example = "750.50")
    val supplierCost: BigDecimal,

    @field:NotNull
    @Schema(description = "Unit of measure for stock.", example = "UNIT")
    val stockUnitType: StockUnitType,

    @field:NotNull
    @Schema(description = "ID of the associated category.", example = "101")
    val categoryId: Long,

    @field:NotNull
    @Schema(description = "ID of the associated brand.", example = "202")
    val brandId: Long,

    @field:NotNull
    @Schema(description = "ID of the associated supplier.", example = "303")
    val supplierId: Long,

    @Schema(description = "List of product images (URLs).")
    val photos: List<Image>? = emptyList(),

    @field:NotNull
    @Schema(description = "Initial status of the product.", example = "ACTIVE")
    val status: ProductStatus
)