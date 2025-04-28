package dev.cnpe.ventescabekotlin.business.application.dto.request

import dev.cnpe.ventescabekotlin.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Request to update basic business details (name, brand message). Fields are optional.")
data class UpdateBusinessBasicsRequest(

    @field:NotBlankIfPresent(message = "Business name cannot be blank if provided.")
    @field:Size(min = 2, max = 50, message = "Business name must be between 2 and 50 characters.")
    @field:Pattern(regexp = "^[\\p{L}\\p{N}\\s&'.-]+$", message = "Business name contains invalid characters")
    @Schema(description = "New official name for the business.", example = "My Updated Store")
    val businessName: String?,

    @field:Size(max = 500, message = "Brand message cannot exceed 500 characters.")
    @Schema(
        description = "New short brand message or slogan. Send empty string to clear.",
        example = "Now with more awesome!"
    )
    val brandMessage: String?
)