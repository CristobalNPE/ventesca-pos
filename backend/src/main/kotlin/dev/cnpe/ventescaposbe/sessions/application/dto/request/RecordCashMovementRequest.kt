package dev.cnpe.ventescaposbe.sessions.application.dto.request

import dev.cnpe.ventescaposbe.sessions.domain.enums.CashMovementReason
import dev.cnpe.ventescaposbe.sessions.domain.enums.CashMovementType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

@Schema(description = "Request to record a manual cash movement (Pay-In or Pay-Out) during a session.")
data class RecordCashMovementRequest(

    @field:NotNull(message = "Movement amount must be provided.")
    @field:Positive(message = "Movement amount must be positive.")
    @Schema(description = "The absolute amount of cash being moved.", example = "20.00", required = true)
    val amount: BigDecimal,

    @field:NotNull(message = "Movement type must be provided.")
    @Schema(description = "The type of movement (PAY_IN or PAY_OUT).", example = "PAY_OUT", required = true)
    val type: CashMovementType,

    @field:NotNull(message = "Reason for movement must be provided.")
    @Schema(description = "The reason for this cash movement.", example = "EXPENSE_PAYMENT", required = true)
    val reason: CashMovementReason,

    @field:Size(max = 255, message = "Notes cannot exceed 255 characters.")
    @Schema(description = "Optional notes describing the movement.", example = "Paid for cleaning supplies.")
    val notes: String? = null
)
