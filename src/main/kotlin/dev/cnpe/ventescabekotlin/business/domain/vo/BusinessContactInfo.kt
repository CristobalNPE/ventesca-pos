package dev.cnpe.ventescabekotlin.business.domain.vo

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

@Schema(description = "Contact information for the business.")
@Embeddable
data class BusinessContactInfo(

    @Schema(
        description = "Primary contact phone number.",
        example = "+1-800-555-0100",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:Size(min = 7, max = 25)
    @Column(name = "phone")
    val phone: String,

    @Schema(
        description = "Primary contact email address.",
        example = "info@business.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Email
    @Column(name = "email")
    val email: String?,

    @Schema(
        description = "Official business website URL.",
        example = "https://www.business.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @field:URL
    @field:Size(max = 255)
    @Column(name = "website")
    val website: String?
)
