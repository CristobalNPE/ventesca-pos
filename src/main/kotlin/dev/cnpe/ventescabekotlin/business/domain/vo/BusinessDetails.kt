package dev.cnpe.ventescabekotlin.business.domain.vo

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

@Schema(description = "Core identifying details of the business")
@Embeddable
data class BusinessDetails(

    @Schema(
        description = "Official name of the business.",
        example = "My Awesome Corp",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotBlank
    @field:Size(min = 2, max = 50)
    @field:Pattern(regexp = "^[\\p{L}\\p{N}\\s&'.-]+$", message = "Business name contains invalid characters")
    @Column(name = "business_name", nullable = false)
    val businessName: String,

    @Schema(
        description = "URL of the business logo.",
        example = "https://cdn.business.com/logo.png",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @field:URL
    @field:Size(max = 255)
    @Column(name = "logo_url")
    val logoUrl: String?,

    @Schema(
        description = "A short brand message or slogan.",
        example = "Quality products since 2023",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @field:Size(max = 500)
    @Column(name = "brand_message", length = 500)
    val brandMessage: String?
)
