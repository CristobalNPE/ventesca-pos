package dev.cnpe.ventescabekotlin.security.ports.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents core identity information retrieved from the Identity Provider.")
data class UserIdentity(
    @Schema(
        description = "The unique identifier assigned by the IdP (e.g., UUID).",
        example = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
        required = true
    )
    val id: String, // IdP's unique ID ('sub' claim), non-nullable

    @Schema(description = "User's primary email address.", example = "jane.doe@business.com", required = true)
    val email: String,

    @Schema(description = "Username used for login (might be same as email).", example = "jdoe")
    val username: String?,

    @Schema(description = "User's first name.", example = "Jane")
    val firstName: String?,

    @Schema(description = "User's last name.", example = "Doe")
    val lastName: String?
)
