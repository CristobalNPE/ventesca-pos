package dev.cnpe.ventescaposbe.sessions.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reason for a manual cash movement (Pay-In or Pay-Out).")
enum class CashMovementReason : DomainEnum {

    @Schema(description = "Cash removed for bank deposit.")
    BANK_DEPOSIT,

    @Schema(description = "Cash added to replenish change.")
    CHANGE_SUPPLY,

    @Schema(description = "Cash removed to pay for a small expense.")
    EXPENSE_PAYMENT,

    @Schema(description = "Adjustment to correct a previous cash error.")
    CORRECTION,

    @Schema(description = "Other miscellaneous reason.")
    OTHER
}