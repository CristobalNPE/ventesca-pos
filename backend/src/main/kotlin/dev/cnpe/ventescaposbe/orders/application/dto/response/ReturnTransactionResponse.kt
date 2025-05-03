package dev.cnpe.ventescaposbe.orders.application.dto.response

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.domain.enums.RefundMethod
import dev.cnpe.ventescaposbe.orders.domain.enums.ReturnStatus
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Detailed response for a processed return transaction.")
data class ReturnTransactionResponse(

    @Schema(description = "Unique ID of the return transaction.")
    val id: Long,

    // TODO: do we need a return number eventually?
    // val returnNumber: String,

    @Schema(description = "ID of the original order.")
    val originalOrderId: Long,

    @Schema(description = "Order number of the original order.")
    val originalOrderNumber: String,

    @Schema(description = "Status of the return.")
    val status: ReturnStatus,

    @Schema(description = "ID of the branch where the return was processed.")
    val branchId: Long,

    @Schema(description = "ID of the user who processed the return.")
    val userIdpId: String,

    @Schema(description = "ID of the customer, if associated.")
    val customerId: Long?,

    @Schema(description = "Timestamp when the return was processed.")
    val returnTimestamp: OffsetDateTime,

    @Schema(description = "Total amount refunded in this transaction.")
    val totalRefundAmount: Money,

    @Schema(description = "Method used for the refund.")
    val refundMethod: RefundMethod,

    @Schema(description = "Optional notes about the return.")
    val notes: String?,

    @Schema(description = "List of items included in this return.")
    val returnedItems: List<ReturnedItemResponse>,

    @Schema(description = "Audit data for the return transaction.")
    val auditData: ResourceAuditData
)