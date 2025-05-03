package dev.cnpe.ventescaposbe.orders.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Method used to issue a refund.")
enum class RefundMethod : DomainEnum {

    @Schema(description = "Refund issued as cash.")
    CASH,

    @Schema(description = "Refund attempted back to the original payment method (e.g., card reversal).")
    ORIGINAL_PAYMENT_METHOD,

    @Schema(description = "Refund issued as store credit.")
    STORE_CREDIT
}