package dev.cnpe.ventescabekotlin.shared.domain.vo

import dev.cnpe.ventescabekotlin.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.validator.constraints.Length

@Schema(description = "Represents a physical address.")
@Embeddable
data class Address(

    @Schema(
        description = "Street name and number.",
        example = "123 Main St",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @field:NotBlankIfPresent
    @field:Length(min = 2, max = 100)
    @Column(name = "street")
    val street: String?,

    @Schema(description = "City name.", example = "Anytown", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @field:NotBlankIfPresent
    @field:Length(min = 2, max = 50)
    @Column(name = "city")
    val city: String?,

    @Schema(description = "Country name or code.", example = "USA", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @field:NotBlankIfPresent
    @field:Length(min = 2, max = 50)
    @Column(name = "country")
    val country: String?,

    @Schema(description = "Postal or ZIP code.", example = "90210", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @field:NotBlankIfPresent
    @field:Length(min = 3, max = 20)
    @Column(name = "postal_code")
    val postalCode: String?
) {
    companion object {
        fun empty(): Address = Address(null, null, null, null)
    }
}