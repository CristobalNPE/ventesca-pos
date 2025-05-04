package dev.cnpe.ventescaposbe.customers.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Summary information for a customer, suitable for lists.")
data class CustomerSummaryResponse(

    @Schema(description = "Unique ID of the customer.")
    val id: Long,

    @Schema(description = "Customer's full name.")
    val fullName: String,

    @Schema(description = "Customer's email address, if available.")
    val email: String?,

    @Schema(description = "Customer's phone number, if available.")
    val phone: String?,

    @Schema(description = "Customer's Tax ID / Personal ID, if available.")
    val taxId: String?,

    @Schema(description = "Indicates if the customer record is active.")
    val isActive: Boolean
)