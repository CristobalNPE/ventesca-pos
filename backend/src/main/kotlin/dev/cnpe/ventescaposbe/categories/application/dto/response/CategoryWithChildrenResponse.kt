package dev.cnpe.ventescaposbe.categories.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Information about a category including its direct subcategories.")
data class CategoryWithChildrenResponse(

    @Schema(
        description = "Unique identifier of the category.",
        example = "201",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val id: Long,

    @Schema(
        description = "Name of the category.",
        example = "Clothing",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val name: String,

    @Schema(
        description = "Unique code assigned to the category.",
        example = "CAT-CLOTH",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val code: String,

    @Schema(
        description = "Color code associated with the category.",
        example = "#3357FF",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val color: String,

    @Schema(
        description = "Indicates if this category has no parent.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isRootCategory: Boolean,

    @Schema(
        description = "Indicates if this is the default category.",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isDefaultCategory: Boolean,

    @Schema(
        description = "Number of products directly associated with this category.",
        example = "15",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val productCount: Long,

    @Schema(
        description = "Set of direct subcategories.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val subcategories: Set<CategorySummaryResponse>
)
