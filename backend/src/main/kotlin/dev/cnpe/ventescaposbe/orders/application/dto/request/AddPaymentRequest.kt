package dev.cnpe.ventescaposbe.orders.application.dto.request

import dev.cnpe.ventescaposbe.business.domain.enums.PaymentMethod
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Schema(description = "Request payload for adding a payment to an order.")
data class AddPaymentRequest(

    @field:Schema(
        description = "The payment method used.",
        example = "CASH",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotNull(message = "Payment method must be provided.")
    val paymentMethod: PaymentMethod,

    @field:Schema(
        description = "The amount being paid with this specific payment.",
        example = "50.00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotNull(message = "Payment amount must be provided.")
    @field:DecimalMin(value = "0.01", message = "Payment amount must be positive.")
//    @field:Digits(integer = 10, fraction = 2, message = "Payment amount format is invalid.") //FIXME: Not sure
    val amount: BigDecimal,

    @field:Schema(
        description = "Optional external transaction reference (e.g., from a card terminal, usually null for cash).",
        example = "txn_123abc",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val transactionReference: String? = null // nullable for cash?
)