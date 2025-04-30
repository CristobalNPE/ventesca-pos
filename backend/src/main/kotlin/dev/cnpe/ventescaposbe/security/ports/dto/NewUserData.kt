package dev.cnpe.ventescaposbe.security.ports.dto

import dev.cnpe.ventescaposbe.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

//FIXME: We might need some validations here
@Schema(description = "Data required to create a new user in the Identity Provider.")
data class NewUserData(

    @Schema(
        description = "User's primary email address (must be unique in IdP).",
        example = "new.admin@business.com",
        required = true
    )
    @field:NotBlank(message = "Email must not be blank.")
    @field:Email(message = "Email should be valid.")
    @field:Size(max = 100, message = "Email cannot exceed 100 characters.")
    val email: String,

    @Schema(description = "User's first name.", example = "Jane")
    @field:NotBlankIfPresent(message = "First name cannot be blank if provided.")
    @field:Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters.")
    val firstName: String?,

    @Schema(description = "User's last name.", example = "Doe")
    @field:NotBlankIfPresent(message = "Last name cannot be blank if provided.")
    @field:Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters.")
    val lastName: String?,

    @Schema(
        description = "Optional initial password. If null, IdP might require user setup.",
        example = "Str0ngP@ssw0rd!"
    )
    @field:NotBlankIfPresent(message = "Password cannot be blank if provided.")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    val initialPassword: String? = null,

    @Schema(
        description = "Optional username. If null, IdP might require user setup.",
        example = "jane.doe"
    )
    @field:NotBlankIfPresent(message = "Username cannot be blank if provided.")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "Username can only contain letters, numbers, underscores, and hyphens."
    )
    val username: String? = null,

    @Schema(description = "Optional set of IDs for the branches the user is initially assigned to.")
    val assignedBranchIds: Set<Long>? = null

)
