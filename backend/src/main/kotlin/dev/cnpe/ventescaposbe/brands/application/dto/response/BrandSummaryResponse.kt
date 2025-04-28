package dev.cnpe.ventescaposbe.brands.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Basic summary information for a brand, suitable for lists.")
data class BrandSummaryResponse(

    @Schema(description = "Brand ID.", requiredMode = Schema.RequiredMode.REQUIRED)
    val id: Long,

    @Schema(description = "Brand name.", requiredMode = Schema.RequiredMode.REQUIRED, example = "Sony")
    val name: String,

    @Schema(description = "Brand code.", requiredMode = Schema.RequiredMode.REQUIRED, example = "SNY")
    val code: String,

    @Schema(description = "Indicates if the brand is the default brand.", requiredMode = Schema.RequiredMode.REQUIRED)
    val isDefault: Boolean,

    @Schema(description = "Timestamp of creation.", requiredMode = Schema.RequiredMode.REQUIRED, format = "date-time")
    val createdAt: OffsetDateTime
)
