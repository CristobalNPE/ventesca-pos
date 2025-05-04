package dev.cnpe.ventescaposbe.orders.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Customer information shown on the receipt, if applicable.")
data class ReceiptCustomerInfo(

    @Schema(description = "Unique ID of the customer.")
    val customerId: Long,

    @Schema(description = "Customer's full name.")
    val fullName: String,

    @Schema(description = "Customer's tax identifier, if available.")
    val taxId: String?
)