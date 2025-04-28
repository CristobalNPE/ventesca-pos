package dev.cnpe.ventescaposbe.security.exception

open class IdpAccessException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
}