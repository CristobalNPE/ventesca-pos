package dev.cnpe.ventescaposbe.tenant.exception

import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.tenant.exception.TenantErrorCode.TENANT_SETUP_FAILED

/**
 * Exception thrown during the tenant creation/setup process (DB creation, migration).
 * Typically results in a 500 Internal Server Error.
 */
class TenantCreationException(
    val tenantIdAttempted: String? = null,
    message: String,
    cause: Throwable? = null
) : DomainException(
    errorCode = TENANT_SETUP_FAILED,
    details = tenantIdAttempted?.let { mapOf("tenantId" to it) },
    parameters = arrayOf(tenantIdAttempted ?: "N/A"),
    message = message,
    cause = cause
)