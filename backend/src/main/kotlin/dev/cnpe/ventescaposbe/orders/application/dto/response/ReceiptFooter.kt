package dev.cnpe.ventescaposbe.orders.application.dto.response

import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(description = "Optional footer information for the receipt.")
data class ReceiptFooter(

    @Schema(description = "A general message (e.g., 'Thank you!').")
    val message: String?,

    @Schema(description = "Information about the store's return policy.")
    val returnPolicy: String?
)