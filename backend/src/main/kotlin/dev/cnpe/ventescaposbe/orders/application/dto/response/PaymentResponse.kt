package dev.cnpe.ventescaposbe.orders.application.dto.response

import dev.cnpe.ventescaposbe.business.domain.enums.PaymentMethod
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.domain.enums.PaymentStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Details of a payment applied to an order.")
data class PaymentResponse(

    @Schema(description = "Unique ID of the payment record.")
    val id: Long,

    @Schema(description = "Method used for payment.")
    val paymentMethod: PaymentMethod,

    @Schema(description = "Amount paid.")
    val amount: Money,

    @Schema(description = "Timestamp of the payment.")
    val paymentTimestamp: OffsetDateTime,

    @Schema(description = "Status of the payment.")
    val status: PaymentStatus,

    @Schema(description = "External transaction reference, if any.")
    val transactionReference: String?
)
