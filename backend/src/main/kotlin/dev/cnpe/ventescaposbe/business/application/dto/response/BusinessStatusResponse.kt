package dev.cnpe.ventescaposbe.business.application.dto.response

import dev.cnpe.ventescaposbe.business.domain.enums.BusinessStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response detailing the current setup/operational status of the business.")
data class BusinessStatusResponse(

    @Schema(
        description = "Current operational status.",
        example = "PENDING",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val status: BusinessStatus,

    @Schema(
        description = "Indicates if the core business entity needs to be created (e.g., first-time setup).",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val needsCreation: Boolean,

    @Schema(
        description = "Indicates if the initial mandatory setup steps are complete.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isSetupComplete: Boolean
)
