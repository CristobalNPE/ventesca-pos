package dev.cnpe.ventescabekotlin.categories.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Basic summary information for a category, suitable for lists.")
data class CategorySummaryResponse(

    @Schema(
        description = "Unique identifier of the category.",
        example = "101",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val id: Long,

    @Schema(description = "Name of the category.", example = "Groceries", requiredMode = Schema.RequiredMode.REQUIRED)
    val name: String,

    @Schema(
        description = "Unique code assigned to the category.",
        example = "CAT-GROC",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val code: String,

    @Schema(
        description = "Color code associated with the category.",
        example = "#33FF57",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val color: String,

    @Schema(
        description = "Indicates if this is the default category.",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isDefault: Boolean,

    @Schema(
        description = "Number of products associated with this category.",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val productCount: Long

)