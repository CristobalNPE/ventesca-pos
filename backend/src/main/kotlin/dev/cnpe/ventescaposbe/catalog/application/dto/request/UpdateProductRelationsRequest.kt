package dev.cnpe.ventescaposbe.catalog.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "Request to update product relationships (category, brand, supplier).")
data class UpdateProductRelationsRequest(

    @field:NotNull
    @field:Positive
    @Schema(description = "ID of the new category.", example = "102")
    val categoryId: Long,

    @field:NotNull
    @field:Positive
    @Schema(description = "ID of the new brand.", example = "203")
    val brandId: Long,

    @field:NotNull
    @field:Positive
    @Schema(description = "ID of the new supplier.", example = "304")
    val supplierId: Long
)