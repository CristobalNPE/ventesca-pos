package dev.cnpe.ventescabekotlin.security.ports.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Data required to create a new user in the Identity Provider.")
data class NewUserData(

    @Schema(
        description = "User's primary email address (must be unique in IdP).",
        example = "new.admin@business.com",
        required = true
    )
    val email: String,

    @Schema(description = "User's first name.", example = "Jane")
    val firstName: String?,

    @Schema(description = "User's last name.", example = "Doe")
    val lastName: String?,

    @Schema(
        description = "Optional initial password. If null, IdP might require user setup.",
        example = "Str0ngP@ssw0rd!"
    )
    val initialPassword: String? = null,

    @Schema(
        description = "Optional username. If null, IdP might require user setup.",
        example = "jane.doe"
    )
    val username: String? = null

)
