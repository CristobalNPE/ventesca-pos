package dev.cnpe.ventescaposbe.orders.application.dto.request

import dev.cnpe.ventescaposbe.orders.domain.enums.RefundMethod
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

@Schema(description = "Request to process a return for one or more items from a completed order.")
data class ProcessReturnRequest(

    @field:NotNull(message = "Original Order ID must be provided.")
    @field:Positive(message = "Original Order ID must be positive.")
    @Schema(description = "ID of the original completed order.", example = "12345")
    val originalOrderId: Long,

    @field:NotEmpty(message = "At least one item must be specified for return.")
    @field:Valid
    @Schema(description = "List of items being returned.")
    val items: List<ReturnItemDetail>,

    @field:NotNull(message = "Refund method must be specified.")
    @Schema(description = "Method used for refunding the customer.", example = "CASH")
    val refundMethod: RefundMethod,

    @field:Size(max = 500, message = "Notes cannot exceed 500 characters.")
    @Schema(description = "Optional notes regarding the return.", example = "Customer unhappy with color.")
    val notes: String? = null
)