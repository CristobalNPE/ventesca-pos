package dev.cnpe.ventescaposbe.orders.application.dto.response

import dev.cnpe.ventescaposbe.currency.vo.Money
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(description = "Details of a single payment made towards the order.")
data class ReceiptPayment(

    @Schema(description="Method used for the payment (e.g., CASH, CREDIT_CARD).")
    val method: String,

    @Schema(description="Amount paid with this specific method.")
    val amount: Money,

    @Schema(description="External transaction reference, if applicable (e.g., card transaction ID).")
    val transactionReference: String?
)