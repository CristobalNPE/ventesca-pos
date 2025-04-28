package dev.cnpe.ventescaposbe.suppliers.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import org.jetbrains.annotations.NotNull

@Schema(description = "Request payload to activate or deactivate a supplier.")
data class UpdateSupplierStatusRequest(

    @field:NotNull
    @Schema(
        description = "Set to true to activate the supplier, false to deactivate.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isActive: Boolean

)
