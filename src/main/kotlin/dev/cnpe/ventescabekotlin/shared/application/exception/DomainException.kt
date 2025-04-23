package dev.cnpe.ventescabekotlin.shared.application.exception

/**
 * Base class for checked exceptions related to domain/business logic violations.
 * These typically map to 4xx HTTP status codes when handled globally.
 *
 * @property errorCode The specific ErrorCode enum constant identifying the error type.
 * @property details Optional map containing structured details about the error (e.g., field name, value).
 * @property parameters Optional parameters used for formatting the user-facing error message.
 * @param message Optional detail message for logging (often derived from errorCode + params).
 * @param cause Optional underlying cause of this exception.
 */
open class DomainException(
    val errorCode: ErrorCode,
    val details: Map<String, Any>? = null,
    vararg val parameters: String,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: "${errorCode.name}(${parameters.joinToString()})", cause) {

}