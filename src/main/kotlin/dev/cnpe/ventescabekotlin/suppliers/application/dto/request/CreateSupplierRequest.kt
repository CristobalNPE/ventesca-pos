package dev.cnpe.ventescabekotlin.suppliers.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

@Schema(description = "Request payload for creating a new supplier.")
data class CreateSupplierRequest(

    @field:NotBlank
    @field:Length(min = 2, max = 50)
    @Schema(
        description = "The primary name of the supplier.",
        example = "Acme Corporation",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val name: String
)