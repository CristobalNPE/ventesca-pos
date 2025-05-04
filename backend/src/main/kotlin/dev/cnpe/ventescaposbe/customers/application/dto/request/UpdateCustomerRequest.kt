package dev.cnpe.ventescaposbe.customers.application.dto.request

import dev.cnpe.ventescaposbe.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

@Schema(description = "Request payload for updating an existing customer. Null fields are ignored.")
data class UpdateCustomerRequest(


    @field:NotBlankIfPresent @field:Size(min = 1, max = 50)
    @Schema(description = "Updated first name.")
    val firstName: String?,

    @field:Size(min = 1, max = 50)
    @Schema(description = "Updated last name. Send empty string to clear.")
    val lastName: String?,

    @field:Email @field:Size(max = 100)
    @Schema(description = "Updated email address. Send empty string to clear.")
    val email: String?,

    @field:Size(min = 7, max = 25)
    @Schema(description = "Updated phone number. Send empty string to clear.")
    val phone: String?,

    @field:Size(min = 2, max = 50)
    @Schema(description = "Updated Tax ID / Personal ID. Send empty string to clear.")
    val taxId: String?,

    @field:Size(max = 100)
    @Schema(description = "Updated street address.")
    val addressStreet: String?,

    @field:Size(max = 50)
    @Schema(description = "Updated city.")
    val addressCity: String?,

    @field:Size(max = 50)
    @Schema(description = "Updated country.")
    val addressCountry: String?,

    @field:Size(max = 20)
    @Schema(description = "Updated postal code.")
    val addressPostalCode: String?,

    @field:Size(max = 500)
    @Schema(description = "Updated notes. Send empty string to clear.")
    val notes: String?
)