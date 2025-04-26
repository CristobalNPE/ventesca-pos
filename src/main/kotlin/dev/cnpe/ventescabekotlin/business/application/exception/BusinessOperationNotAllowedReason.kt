package dev.cnpe.ventescabekotlin.business.application.exception

import dev.cnpe.ventescabekotlin.shared.application.exception.OperationNotAllowedReason
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reasons why a business operation is not allowed.")
enum class BusinessOperationNotAllowedReason : OperationNotAllowedReason {

    @Schema(description = "User performing the action is not authorized to manage users for this business.")
    NOT_AUTHORIZED_TO_MANAGE_USERS,

    @Schema(description = "Cannot assign the requested roles (e.g., assigning ADMIN role).")
    CANNOT_ASSIGN_ROLES,

    @Schema(description = "Cannot add more users to the business, as the maximum number of users has been reached.")
    USER_LIMIT_REACHED
}