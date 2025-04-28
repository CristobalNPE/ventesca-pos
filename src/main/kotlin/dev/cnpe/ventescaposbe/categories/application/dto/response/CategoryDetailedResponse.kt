package dev.cnpe.ventescaposbe.categories.application.dto.response

import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Complete detailed information about a category, including parent, children, and metadata.")
data class CategoryDetailedResponse(

    @Schema(
        description = "Unique identifier of the category.",
        example = "305",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val id: Long,

    @Schema(
        description = "Name of the category.",
        example = "Smartphones",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val name: String,

    @Schema(
        description = "Detailed description of the category.",
        example = "Mobile phones with advanced computing capabilities.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val description: String?,

    @Schema(
        description = "Unique code assigned to the category.",
        example = "CAT-SMART",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val code: String,

    @Schema(
        description = "Color code associated with the category.",
        example = "#F0E68C",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val color: String,

    @Schema(
        description = "Indicates if this category has no parent.",
        example = "false",
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
        description = "Basic information about the parent category (null if root).",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val parent: CategorySummaryResponse?,

    @Schema(
        description = "Set of direct subcategories.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val subcategories: Set<CategorySummaryResponse>,

    @Schema(
        description = "Number of products directly associated with this category.",
        example = "25",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val productCount: Long,

    @Schema(
        description = "Audit information.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val auditData: ResourceAuditData
)
