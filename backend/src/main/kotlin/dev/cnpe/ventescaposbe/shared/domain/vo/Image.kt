package dev.cnpe.ventescaposbe.shared.domain.vo

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

@Schema(description = "An image associated with a resource, identified by its URL.")
@Embeddable
data class Image(
    @Schema(
        description = "The URL of the image.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "https://example.com/images/product.jpg"
    )
    @Column(name = "image_url", length = 500, nullable = false)
    @field:NotBlank
    @field:URL
    val url: String
)
