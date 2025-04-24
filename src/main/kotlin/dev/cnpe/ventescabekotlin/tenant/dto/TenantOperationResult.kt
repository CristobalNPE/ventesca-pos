package dev.cnpe.ventescabekotlin.tenant.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Result of a tenant operation.")
data class TenantOperationResult(
    @Schema(
        description = "Human readable sentence that represents the result of the operation.",
        example = "Schema update process finished"
    )
    val message: String,

    @Schema(description = "Duration of the operation in milliseconds.")
    val durationMillis: Long
)
