package dev.cnpe.ventescaposbe.customers.application.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Basic identifying information for a customer.")
data class CustomerBasicInfo(

    @Schema(description = "Unique ID of the customer.")
    val id: Long,

    @Schema(description = "Customer's full name.")
    val fullName: String,

    @Schema(description = "Customer's email address, if available.")
    val email: String?,

    @Schema(description = "Customer's phone number, if available.")
    val phone: String?
)
