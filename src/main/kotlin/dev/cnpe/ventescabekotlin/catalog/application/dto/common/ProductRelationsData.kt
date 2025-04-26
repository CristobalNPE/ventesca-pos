package dev.cnpe.ventescabekotlin.catalog.application.dto.common

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "IDs representing product relationships.")
data class ProductRelationsData(

    @Schema(description = "ID of the associated category.")
    val categoryId: Long?,

    @Schema(description = "ID of the associated brand.")
    val brandId: Long?,

    @Schema(description = "ID of the associated supplier.")
    val supplierId: Long?
)