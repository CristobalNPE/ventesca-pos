package dev.cnpe.ventescaposbe.orders.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Receipt header information.")
data class ReceiptHeader(

    @Schema(description = "Name of the business conducting the sale.")
    val businessName: String,

    @Schema(description = "Name of the specific branch where the sale occurred.")
    val branchName: String,

    @Schema(description = "Formatted address of the branch.")
    val branchAddress: String?,

    @Schema(description = "Contact phone number of the branch.")
    val branchPhone: String?,

    @Schema(description = "Unique identifier for the order.")
    val orderNumber: String,

    @Schema(description = "Date and time the order was placed/completed.")
    val orderTimestamp: OffsetDateTime,

    @Schema(description = "Identifier of the cashier who processed the order.")
    val cashierId: String,

    @Schema(description = "Display name of the cashier, if available.")
    val cashierName: String?,

    @Schema(description = "Identifier of the register session, if available.")
    val sessionNumber: String?
)
