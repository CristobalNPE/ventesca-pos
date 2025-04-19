package dev.cnpe.ventescabekotlin.categories.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

@Schema(description = "Request payload for creating a new category.")
data class CreateCategoryRequest(

    @field:NotBlank(message = "Name must not be blank.")
    @field:Length(min = 2, max = 50, message = "Name must be between 2 and 50 characters.")
    @Schema(description = "Name of the category.", example = "Electronics", requiredMode = Schema.RequiredMode.REQUIRED)
    val name: String
)
