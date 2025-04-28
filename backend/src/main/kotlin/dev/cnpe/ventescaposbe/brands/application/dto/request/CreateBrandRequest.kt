package dev.cnpe.ventescaposbe.brands.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

@Schema(description = "Request to create a new brand.")
data class CreateBrandRequest(

    @field:NotBlank(message = "Name must not be blank.")
    @field:Length(min = 2, max = 20, message = "Name must be between 2 and 20 characters.")
    @Schema(description = "Brand name.", example = "Sony")
    val name: String
)
