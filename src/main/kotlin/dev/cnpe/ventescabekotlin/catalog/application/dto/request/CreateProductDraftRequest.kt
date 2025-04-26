package dev.cnpe.ventescabekotlin.catalog.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

@Schema(description = "Request to create a new product draft with minimal info.")
data class CreateProductDraftRequest(

    @field:NotBlank
    @field:Length(min = 3, max = 70)
    @Schema(description = "Display name of the product.", example = "Basic Widget")
    val name: String,

    @field:NotBlank
    @field:Length(min = 3, max = 70)
    @Schema(description = "Barcode (EAN, UPC, etc.). Must be unique.", example = "1234567890128")
    val barcode: String
)