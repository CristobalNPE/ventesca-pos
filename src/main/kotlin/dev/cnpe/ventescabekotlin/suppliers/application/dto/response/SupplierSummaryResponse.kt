package dev.cnpe.ventescabekotlin.suppliers.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Basic summary information for a supplier, suitable for lists.")
data class SupplierSummaryResponse(

    @Schema(
        description = "Unique identifier of the supplier.",
        example = "55",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val id: Long,

    @Schema(
        description = "The legal business name of the supplier.",
        example = "Global Tech Supplies",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val name: String,

    @Schema(
        description = "Indicates if the supplier is currently active.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isActive: Boolean,

    @Schema(
        description = "Indicates if this is the default supplier.",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isDefault: Boolean,

    @Schema(
        description = "Timestamp when the supplier was created.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val createdAt: OffsetDateTime
)
