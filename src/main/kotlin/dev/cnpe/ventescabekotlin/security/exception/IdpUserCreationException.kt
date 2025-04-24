package dev.cnpe.ventescabekotlin.security.exception

class IdpUserCreationException(
    val emailAttempted: String?,
    message: String,
    cause: Throwable? = null
) : IdpAccessException("Failed to create user ${emailAttempted ?: "N/A"}: $message", cause) {
}