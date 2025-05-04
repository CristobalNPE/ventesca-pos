package dev.cnpe.ventescaposbe.sessions.application.dto.response

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.sessions.domain.enums.CashMovementReason
import dev.cnpe.ventescaposbe.sessions.domain.enums.CashMovementType
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Details of a recorded manual cash movement.")
data class CashMovementResponse(

    @Schema(description = "Unique ID of the cash movement record.")
    val id: Long,
    @Schema(description = "ID of the session this movement belongs to.")
    val sessionId: Long,
    @Schema(description = "Timestamp when the movement occurred.")
    val timestamp: OffsetDateTime,
    @Schema(description = "Type of movement (PAY_IN or PAY_OUT).")
    val type: CashMovementType,
    @Schema(description = "Amount of cash moved.")
    val amount: Money,
    @Schema(description = "Reason for the movement.")
    val reason: CashMovementReason,
    @Schema(description = "Optional notes.")
    val notes: String?,
    @Schema(description = "ID of the user who recorded the movement.")
    val userIdpId: String,
    @Schema(description = "Audit information for the movement.")
    val auditData: ResourceAuditData
)