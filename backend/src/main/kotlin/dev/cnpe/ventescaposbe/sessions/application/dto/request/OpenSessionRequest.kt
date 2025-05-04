package dev.cnpe.ventescaposbe.sessions.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

@Schema(description = "Request to open a new register session.")
data class OpenSessionRequest(

    @field:NotNull(message = "Branch ID must be provided.")
    @field:Positive(message = "Branch ID must be positive.")
    @Schema(description = "ID of the branch where the session is being opened.", example = "101", required = true)
    val branchId: Long,

    @field:NotNull(message = "Opening float amount must be provided.")
    @field:DecimalMin(value = "0.0", message = "Opening float cannot be negative.")
    @Schema(description = "The initial cash amount (float) in the register.", example = "50.00", required = true)
    val openingFloatAmount: BigDecimal
)
