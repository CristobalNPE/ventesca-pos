package dev.cnpe.ventescaposbe.reporting.application.dto.response

import dev.cnpe.ventescaposbe.business.domain.enums.PaymentMethod
import dev.cnpe.ventescaposbe.currency.vo.Money
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Report summarizing sales broken down by payment method.")
data class SalesByPaymentMethodReport(
    val breakdown: List<PaymentMethodSummary>
)

@Schema(description = "Summary for a single payment method.")
data class PaymentMethodSummary(
    @Schema(description = "The payment method used.")
    val paymentMethod: PaymentMethod,

    @Schema(description = "Total amount collected via this payment method.")
    val totalAmount: Money,

    @Schema(description = "Number of transactions using this payment method.")
    val transactionCount: Int
)