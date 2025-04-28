package dev.cnpe.ventescaposbe.tenant.exception

import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.tenant.exception.TenantErrorCode.TENANT_RESOLUTION_FAILED

/**
 * Exception thrown when the tenant for the current context (e.g., authenticated user)
 * cannot be found or resolved. Typically results in a 403 Forbidden.
 */
class TenantNotFoundException(
    val identifierTried: Any? = null, // user email or token claim
    message: String = "Tenant could not be resolved for the current context.", // default msg
    cause: Throwable? = null
) : DomainException(
    errorCode = TENANT_RESOLUTION_FAILED,
    details = identifierTried?.let { mapOf("identifier" to it) },
    parameters = arrayOf(identifierTried?.toString() ?: "unknown"),
    message = message,
    cause = cause
)