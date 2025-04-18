package dev.cnpe.ventescabekotlin.brands.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Length

@Schema(description = "Request to update a brand.")
data class UpdateBrandRequest(

//    @field:NotBlankIfPresent // We don't have this annotation migrated yet.
    @field:Length(min = 2, max = 20, message = "Name must be between 2 and 20 characters.")
    @Schema(description = "Brand name.", example = "Sony")
    val name: String
)