package dev.cnpe.ventescabekotlin.tenant

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

class DynamicTenantAwareRoutingSource(
    private val tenantDataSource: TenantDataSource
) : AbstractRoutingDataSource() {

    companion object {
        const val DEFAULT_TENANT_KEY = "default"
    }


    /**
     * Determines the lookup key for the current DataSource based on TenantContext.
     * Returns the tenant ID string or a default key if no tenant is set.
     * This key is used by AbstractRoutingDataSource's internal map *if* determineTargetDataSource is NOT overridden.
     * Since we override determineTargetDataSource, this method's return value is less critical
     * but still good practice to implement correctly.
     */
    override fun determineCurrentLookupKey(): Any? {
        val tenantId = TenantContext.getCurrentTenant()
        val lookupKey = tenantId ?: DEFAULT_TENANT_KEY
        log.trace { "Determined current lookup key: $lookupKey" }
        return lookupKey
    }

    /**
     * Directly determines and returns the target DataSource based on TenantContext.
     * This overrides the default lookup mechanism of AbstractRoutingDataSource
     * and allows for dynamic creation/retrieval via TenantDataSource.
     */
    override fun determineTargetDataSource(): DataSource {
        val tenantId = TenantContext.getCurrentTenant()

        return if (tenantId != null) {
            log.debug { "Routing to DataSource for tenant: $tenantId" }
            tenantDataSource.getDataSource(tenantId)
        } else {
            log.debug { "No tenant context found, routing to default DataSource." }
            resolvedDefaultDataSource ?: throw IllegalStateException("Default target DataSource is not configured")
        }
    }
}