package dev.cnpe.ventescabekotlin.security.exception

class IdpRoleAssignmentException(
    message: String,
    cause: Throwable? = null
) : IdpAccessException("Failed to assign roles to user", cause) {
}