package dev.cnpe.ventescaposbe.sessions.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

@Schema(description = "Request to close an open register session.")
data class CloseSessionRequest(

    @field:NotNull(message = "Counted cash amount must be provided.")
    @field:DecimalMin(value = "0.0", message = "Counted cash cannot be negative.")
    @Schema(
        description = "The total cash amount physically counted in the register drawer at closing.",
        example = "375.50",
        required = true
    )
    val countedCashAmount: BigDecimal,

    @field:Size(max = 500, message = "Notes cannot exceed 500 characters.")
    @Schema(description = "Optional closing notes.", example = "Register balanced.")
    val notes: String? = null
)