package dev.cnpe.ventescabekotlin.shared.application.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents information about a specific enum type, including its possible values.")
data class EnumInfoResponse(
    @Schema(description = "The simple name of the enum type.", example = "BusinessStatus")
    val enumName: String,

    @Schema(description = "List of possible values for this enum type, with localized text.")
    val values: List<EnumValueInfo>
)