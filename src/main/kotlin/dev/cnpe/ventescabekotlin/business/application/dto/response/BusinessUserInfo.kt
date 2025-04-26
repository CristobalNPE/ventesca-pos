package dev.cnpe.ventescabekotlin.business.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "User information for a business.")
data class BusinessUserInfo(

    @Schema(description = "Unique identifier for the user.")
    val idpUserId: String,

    @Schema(description = "Email address for the user.")
    val email: String?,

    @Schema(description = "Display name for the user. Combination of names or username")
    val displayName: String?,

    @Schema(description = "Roles assigned to the user.")
    val roles: Set<String>
)
