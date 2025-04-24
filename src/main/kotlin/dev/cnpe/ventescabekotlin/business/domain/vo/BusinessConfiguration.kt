package dev.cnpe.ventescabekotlin.business.domain.vo


import com.fasterxml.jackson.annotation.JsonIgnore
import dev.cnpe.ventescabekotlin.business.domain.enums.PaymentMethod
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import kotlin.collections.isNotEmpty

@Schema(description = "Configuration settings for the business.")
@Embeddable
data class BusinessConfiguration(
    @Schema(
        description = "Default currency code (ISO 4217).",
        example = "USD",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Column(name = "currency_code")
    @field:NotBlank
    @field:Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be a valid 3-letter ISO 4217 code")
    val currencyCode: String,

    @Schema(
        description = "Default tax percentage applied.",
        example = "8.25",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Column(name = "tax_percentage")
    @field:NotNull
    @field:DecimalMin(value = "0.0", inclusive = true)
    @field:Digits(integer = 3, fraction = 2)
    val taxPercentage: BigDecimal,

    @Schema(description = "Set of accepted payment methods.", requiredMode = Schema.RequiredMode.REQUIRED)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "business_payment_methods",
        joinColumns = [JoinColumn(name = "business_id")]
    )
    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    @field:NotNull
    @field:Size(min = 1, message = "At least one payment method must be selected")
    val acceptedPaymentMethods: Set<PaymentMethod> = emptySet()
) {
    @get:JsonIgnore
    val isValid: Boolean
        get() = currencyCode.isNotBlank() &&
                taxPercentage >= BigDecimal.ZERO &&
                acceptedPaymentMethods.isNotEmpty()
}
