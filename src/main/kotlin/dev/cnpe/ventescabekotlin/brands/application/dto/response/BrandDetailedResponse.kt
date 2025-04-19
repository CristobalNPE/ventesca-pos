package dev.cnpe.ventescabekotlin.brands.application.dto.response

import dev.cnpe.ventescabekotlin.shared.application.dto.ResourceAuditData
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Detailed information for a brand.")
data class BrandDetailedResponse(

    @Schema(description = "Brand ID.", requiredMode = Schema.RequiredMode.REQUIRED)
    val id: Long,

    @Schema(description = "Brand name.", requiredMode = Schema.RequiredMode.REQUIRED, example = "Sony")
    val name: String,

    @Schema(description = "Brand code.", requiredMode = Schema.RequiredMode.REQUIRED, example = "SNY")
    val code:String,

    @Schema(description = "Audit information.", requiredMode = Schema.RequiredMode.REQUIRED)
    val isDefault: Boolean,

    @Schema(description = "Audit information.", requiredMode = Schema.RequiredMode.REQUIRED)
    val auditData: ResourceAuditData
)
