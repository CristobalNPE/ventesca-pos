package dev.cnpe.ventescabekotlin.suppliers.application.dto.request

import dev.cnpe.ventescabekotlin.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.URL

@Schema(description = "Request payload for updating supplier details. All fields are optional (provide only fields to be updated).")
data class UpdateSupplierRequest(

    @field:NotBlankIfPresent
    @field:Length(min = 2, max = 50)
    @Schema(
        description = "Updated legal business name.",
        example = "Acme Corp Ltd.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val name: String?,

    @field:NotBlankIfPresent
    @Length(min = 1, max = 50)
    @Schema(
        description = "Updated first name of the representative.",
        example = "John",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val representativeFirstName: String?,

    @field:NotBlankIfPresent
    @Length(min = 1, max = 50)
    @Schema(
        description = "Updated last name of the representative.",
        example = "Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val representativeLastName: String?,

    @field:NotBlankIfPresent
    @Length(min = 2, max = 50)
    @Schema(
        description = "Updated personal identification number of the representative.",
        example = "ID123456",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val representativePersonalId: String?,

    @field:NotBlankIfPresent
    @Length(min = 7, max = 20)
    @Schema(
        description = "Updated primary phone number.",
        example = "+1-555-123-4567",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val phoneNumber: String?,

    @field:NotBlankIfPresent
    @Email
    @Schema(
        description = "Updated primary email address.",
        example = "contact@acmecorp.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val email: String?,

    @field:NotBlankIfPresent
    @field:URL
    @Length(min = 5, max = 100)
    @Schema(
        description = "Updated company website URL.",
        example = "https://www.acmecorp.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val website: String?,

    @field:NotBlankIfPresent
    @Length(min = 2, max = 100)
    @Schema(
        description = "Updated street address.",
        example = "123 Main St",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val addressStreet: String?,

    @field:NotBlankIfPresent
    @Length(min = 2, max = 50)
    @Schema(description = "Updated city.", example = "Anytown", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val addressCity: String?,

    @field:NotBlankIfPresent
    @Length(min = 2, max = 50)
    @Schema(description = "Updated country.", example = "USA", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val addressCountry: String?,

    @field:NotBlankIfPresent
    @Length(min = 3, max = 20)
    @Schema(description = "Updated postal code.", example = "90210", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val addressPostalCode: String?
)
