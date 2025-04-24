package dev.cnpe.ventescabekotlin.security.exception

open class IdpAccessException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
}