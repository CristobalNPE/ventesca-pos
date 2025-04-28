package dev.cnpe.ventescaposbe.shared.domain.vo

import dev.cnpe.ventescaposbe.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.Email
import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.URL

@Schema(description = "Represents contact information.")
@Embeddable
data class ContactInfo(

    @Schema(
        description = "Primary phone number.",
        example = "+1-555-123-4567",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @field:NotBlankIfPresent
    @field:Length(min = 7, max = 20)
    @Column(name = "phone_number")
    val phoneNumber: String?,

    @Schema(
        description = "Primary email address.",
        example = "contact@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @field:NotBlankIfPresent
    @field:Email
    @Column(name = "email")
    val email: String?,

    @Schema(
        description = "Website URL.",
        example = "https://www.example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @field:NotBlankIfPresent
    @field:URL
    @field:Length(min = 5, max = 100)
    @Column(name = "website")
    val website: String?
) {
    companion object {
        fun empty(): ContactInfo = ContactInfo(null, null, null)
    }

}