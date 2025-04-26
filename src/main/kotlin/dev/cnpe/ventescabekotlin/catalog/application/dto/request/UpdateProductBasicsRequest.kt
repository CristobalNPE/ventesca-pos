package dev.cnpe.ventescabekotlin.catalog.application.dto.request

import dev.cnpe.ventescabekotlin.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Length

@Schema(description = "Request to update basic product information (name, description).")
data class UpdateProductBasicsRequest(

    @field:NotBlankIfPresent
    @field:Length(min = 3, max = 70)
    @get:Schema(description = "New display name for the product.", example = "Laptop Pro 32GB")
    val name: String?,

    @field:Length(min = 3, max = 1000)
    @get:Schema(
        description = "New product description. Send empty string to clear.",
        example = "Updated description..."
    )
    val description: String?
)