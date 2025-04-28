package dev.cnpe.ventescaposbe.tenant

import com.zaxxer.hikari.HikariDataSource
import dev.cnpe.ventescaposbe.tenant.config.TenantDataSourceProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

private val log = KotlinLogging.logger { }

@Component
class TenantDataSource(
    private val properties: TenantDataSourceProperties
) {
    private val dataSources: MutableMap<String, DataSource> = ConcurrentHashMap()

    /**
     * Retrieves the DataSource for the given tenantId.
     * Creates and caches the DataSource if it doesn't exist.
     *
     * @param tenantId The identifier of the tenant.
     * @return The DataSource for the tenant.
     * @throws IllegalArgumentException if tenantId is blank.
     */
    fun getDataSource(tenantId: String): DataSource {
        require(tenantId.isNotBlank()) { "Tenant ID cannot be blank" }

        return dataSources.computeIfAbsent(tenantId) { key ->
            log.info { "Creating new DataSource for tenant: $key" }
            createDataSource(key)
        }
    }

    /**
     * Explicitly adds a tenant DataSource to the cache. Typically called during tenant creation.
     * Ensures the DataSource is ready before it might be needed.
     *
     * @param tenantId The identifier of the tenant to add.
     */
    fun addTenant(tenantId: String) {
        require(tenantId.isNotBlank()) { "Tenant ID cannot be blank" }
        dataSources.computeIfAbsent(tenantId) { key ->
            log.info { "Preemptively creating DataSource for added tenant: $key" }
            createDataSource(key)
        }
    }

    /**
     * Removes a tenant's DataSource from the cache and closes it.
     * Should be called when a tenant is deactivated or deleted.
     *
     * @param tenantId The identifier of the tenant to remove.
     */
    fun removeTenant(tenantId: String) {
        val dataSource = dataSources.remove(tenantId)
        if (dataSource != null) {
            log.info { "Removing and closing DataSource for tenant: $tenantId" }
            (dataSource as? HikariDataSource)?.close()
        } else {
            log.warn { "Attempted to remove DataSource for non-cached tenant: $tenantId" }
        }
    }

    /**
     * Creates a new HikariDataSource instance for a specific tenant.
     */
    private fun createDataSource(tenantId: String): DataSource {
        val resolvedUrl = properties.urlTemplate.replace("{tenant}", tenantId)
        log.debug { "Resolved JDBC URL for tenant '$tenantId': $resolvedUrl" }

        val ds = HikariDataSource()
        ds.jdbcUrl = resolvedUrl
        ds.username = properties.username
        ds.password = properties.password
        ds.poolName = "${properties.hikari.poolNamePrefix}$tenantId"
        ds.maximumPoolSize = properties.hikari.maximumPoolSize

        return ds
    }
}