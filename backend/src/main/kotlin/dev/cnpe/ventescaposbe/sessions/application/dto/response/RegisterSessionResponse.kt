package dev.cnpe.ventescaposbe.sessions.application.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.sessions.domain.enums.SessionStatus
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Detailed information about a register session.")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RegisterSessionResponse(

    @Schema(description = "Unique ID of the session.")
    val id: Long,
    @Schema(description = "System-generated session number.")
    val sessionNumber: String,
    @Schema(description = "ID of the branch.")
    val branchId: Long,
    @Schema(description = "ID of the user (cashier) who owns the session.")
    val userIdpId: String,
    @Schema(description = "Optional ID of the physical register.")
    val registerId: String?,
    @Schema(description = "Current status of the session.")
    val status: SessionStatus,
    @Schema(description = "Timestamp when the session was opened.")
    val openingTime: OffsetDateTime,
    @Schema(description = "Timestamp when the session was closed (null if open).")
    val closingTime: OffsetDateTime?,
    @Schema(description = "Declared cash float at the start of the session.")
    val openingFloat: Money,
    @Schema(description = "Declared cash counted at the end of the session (null if open).")
    val closingCountedCash: Money?,

    @Schema(description = "Total cash received from sales during the session.")
    val calculatedCashSales: Money?,
    @Schema(description = "Total cash issued for refunds during the session.")
    val calculatedCashRefunds: Money?,
    @Schema(description = "Total cash manually added (Pay-Ins) during the session.")
    val calculatedPayIns: Money?,
    @Schema(description = "Total cash manually removed (Pay-Outs) during the session.")
    val calculatedPayOuts: Money?,
    @Schema(description = "Expected cash in register at closing (calculated).")
    val calculatedExpectedCash: Money?,
    @Schema(description = "Difference between counted and expected cash at closing.")
    val cashVariance: Money?,
    @Schema(description = "Total sales amount (all payment types) during the session.")
    val totalSalesAmount: Money?,
    @Schema(description = "Total refund amount (all refund methods) during the session.")
    val totalRefundAmount: Money?,

    @Schema(description = "Optional notes for the session.")
    val notes: String?,
    @Schema(description = "Audit information for the session.")
    val auditData: ResourceAuditData

    // TOdo: Maybe add list of CashMovementResponse here? Or separate endpoint?
)