package dev.cnpe.ventescabekotlin.categories.application.dto.request

import dev.cnpe.ventescabekotlin.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Length

@Schema(description = "Request payload for updating an existing category. All fields are optional.")
data class UpdateCategoryRequest(

    @field:NotBlankIfPresent
    @field:Length(min = 2, max = 50, message = "Name must be between 2 and 50 characters.")
    @Schema(
        description = "Updated name of the category.",
        example = "Consumer Electronics",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val name: String?,

    @field:NotBlankIfPresent
    @field:Length(max = 150, message = "Description must be at most 150 characters.")
    @Schema(
        description = "Updated description for the category.",
        example = "Devices and gadgets for everyday use.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val description: String?,

    @field:NotBlankIfPresent
    @field:Length(max = 7, message = "Color must be at most 7 characters.")
    @Schema(
        description = "Updated color code for the category (e.g., hex format).",
        example = "#FF5733",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val color: String?

)
