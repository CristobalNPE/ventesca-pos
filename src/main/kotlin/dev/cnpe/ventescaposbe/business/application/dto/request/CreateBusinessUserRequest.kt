package dev.cnpe.ventescaposbe.business.application.dto.request

import dev.cnpe.ventescaposbe.security.ports.dto.NewUserData
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid

@Schema(description = "Request to create a new business user.")
data class CreateBusinessUserRequest(

    @Schema(description = "User data for the new user.")
    @field:Valid
    val userData: NewUserData,

    @Schema(description = "Roles to be assigned to the new user.")
    val roles: Set<String> = setOf("SELLER")

)
