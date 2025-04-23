package dev.cnpe.ventescabekotlin.tenant

import io.github.oshai.kotlinlogging.KotlinLogging
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

/**
 * Resolves the current tenant identifier for Hibernate multi-tenancy.
 * Retrieves the tenant ID from the TenantContext ThreadLocal.
 */
@Component
class TenantSchemaResolver : CurrentTenantIdentifierResolver<String> {

    companion object {
        // The constant used when no tenant context is found (e.g., during startup or background tasks)
        // This MUST match the key used for the default datasource in DataSourceConfig
        const val DEFAULT_TENANT_ID = "default"
    }

    /**
     * Returns the current tenant identifier from TenantContext, or a default value.
     */
    override fun resolveCurrentTenantIdentifier(): String? {
        val tenantId = TenantContext.getCurrentTenant() ?: DEFAULT_TENANT_ID
        log.trace { "Resolved current tenant identifier: $tenantId" }
        return tenantId
    }

    /**
     * Indicates whether Hibernate should validate that the resolved tenant identifier
     * matches the identifier associated with any existing session. Returning true is safer.
     */
    override fun validateExistingCurrentSessions(): Boolean {
        return true
    }

}