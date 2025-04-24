package dev.cnpe.ventescabekotlin.shared.application.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents information about a specific enum constant, including localized text.")
data class EnumValueInfo(
    @Schema(description = "The canonical, language-independent name of the enum constant.", example = "ACTIVE")
    val value: String,

    @Schema(description = "The localized, user-friendly display name for the enum constant.", example = "Active")
    val name: String,

    @Schema(description = "The localized description for the enum constant (optional).", example = "Fully operational.")
    val description: String?
)