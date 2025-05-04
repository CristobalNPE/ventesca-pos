package dev.cnpe.ventescaposbe.customers.application.dto.request

import dev.cnpe.ventescaposbe.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Request payload for creating a new customer.")
data class CreateCustomerRequest(

    @field:NotBlank(message = "First name cannot be blank.")
    @field:Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters.")
    @Schema(description = "Customer's first name.", example = "Jhon", required = true)
    val firstName: String,

    @field:NotBlankIfPresent(message = "Last name cannot be blank if provided.")
    @field:Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters.")
    @Schema(description = "Customer's last name.", example = "Doe")
    val lastName: String?,

    @field:NotBlankIfPresent(message = "Email cannot be blank if provided.")
    @field:Email(message = "Email should be valid.")
    @field:Size(max = 100, message = "Email cannot exceed 100 characters.")
    @Schema(
        description = "Customer's email address (optional, should be unique if provided).",
        example = "jhon.doe@email.com"
    )
    val email: String?,

    @field:NotBlankIfPresent(message = "Phone cannot be blank if provided.")
    @field:Size(min = 7, max = 25, message = "Phone number length invalid.")
    @Schema(description = "Customer's phone number (optional).", example = "+19876543210")
    val phone: String?,

    @field:NotBlankIfPresent(message = "Tax ID cannot be blank if provided.")
    @field:Size(min = 2, max = 50, message = "Tax ID length invalid.")
    @Schema(
        description = "Customer's Tax ID / Personal ID (optional, should be unique if provided).",
        example = "12345678-9"
    )
    val taxId: String?,

    @field:NotBlankIfPresent @field:Size(max = 100)
    @Schema(description = "Street address.", example = "123 Main St")
    val addressStreet: String?,

    @field:NotBlankIfPresent @field:Size(max = 50)
    @Schema(description = "City.", example = "Anytown")
    val addressCity: String?,

    @field:NotBlankIfPresent @field:Size(max = 50)
    @Schema(description = "Country.", example = "USA")
    val addressCountry: String?,

    @field:NotBlankIfPresent @field:Size(max = 20)
    @Schema(description = "Postal code.", example = "90210")
    val addressPostalCode: String?,

    @field:Size(max = 500)
    @Schema(description = "Optional notes about the customer.")
    val notes: String?
)