package dev.cnpe.ventescabekotlin.business.application.dto.request

import dev.cnpe.ventescabekotlin.business.domain.enums.PaymentMethod
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.math.BigDecimal

@Schema(description = "Request to update business configuration (currency, tax, payment methods). Fields are optional.")
data class UpdateBusinessConfigurationRequest(

    @field:Pattern(regexp = "^[A-Z]{3}$", message = "Must be a 3-letter uppercase ISO 4217 code")
    @Schema(description = "New default currency code (ISO 4217).", example = "EUR")
    val currencyCode: String?, // TODO: Add validation against Currency table? Deferred.

    @field:DecimalMin(value = "0.0", inclusive = true, message = "Tax percentage cannot be negative.")
    @field:DecimalMax(value = "100.0", inclusive = true, message = "Tax percentage cannot exceed 100.")
    @field:Digits(integer = 3, fraction = 2, message = "Tax percentage format invalid (e.g., 7.50).")
    @Schema(description = "New default tax percentage (0-100).", example = "8.0")
    val taxPercentage: BigDecimal?,

    @field:Size(min = 1, message = "At least one payment method must be provided if updating.")
    @Schema(
        description = "New set of accepted payment methods (replaces the existing set).",
        example = "[\"CASH\", \"DEBIT_CARD\"]"
    )
    val acceptedPaymentMethods: Set<PaymentMethod>?
)