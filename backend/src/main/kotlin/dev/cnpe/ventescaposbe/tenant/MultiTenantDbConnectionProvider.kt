package dev.cnpe.ventescaposbe.tenant

import io.github.oshai.kotlinlogging.KotlinLogging
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

/**
 * Provides Hibernate with the correct JDBC Connection based on the resolved tenant identifier.
 * Uses TenantDataSource to retrieve the specific DataSource for a tenant
 * and the master DataSource for generic connections.
 */
@Component
class MultiTenantDbConnectionProvider(
    private val tenantDataSource: TenantDataSource,
    @Qualifier("masterDataSource") private val masterDataSource: DataSource
) : MultiTenantConnectionProvider<String> {

    /**
     * Provides a connection from the master/default pool when Hibernate needs
     * a connection without a specific tenant context.
     */
    override fun getAnyConnection(): Connection? {
        log.debug { "Getting connection from default master pool." }
        return masterDataSource.connection
    }

    /**
     * Releases a connection obtained via getAnyConnection().
     */
    @Throws(SQLException::class)
    override fun releaseAnyConnection(connection: Connection) {
        try {
            connection.close()
            log.trace { "Released 'any' connection." }
        } catch (e: SQLException) {
            log.error(e) { "Error closing 'any' connection." }
            throw e
        }
    }

    /**
     * Provides a connection specific to the given tenant identifier.
     * Retrieves the tenant-specific DataSource from TenantDataSource.
     */
    @Throws(SQLException::class)
    override fun getConnection(tenantIdentifier: String): Connection {
        log.debug { "Getting JDBC connection for tenant identifier: $tenantIdentifier" }
        val specificDataSource = tenantDataSource.getDataSource(tenantIdentifier)
        val connection = specificDataSource.connection
        log.trace { "Obtained connection for tenant '$tenantIdentifier': $connection" }
        return connection
        // Note: We are NOT setting TenantContext here. Hibernate calls this *after*
        // TenantSchemaResolver has determined the identifier.
    }

    /**
     * Releases a connection obtained via getConnection(tenantIdentifier).
     */
    @Throws(SQLException::class)
    override fun releaseConnection(tenantIdentifier: String, connection: Connection) {
        log.trace { "Releasing connection for tenant: $tenantIdentifier" }
        try {
            connection.close()
            log.trace { "Released connection for tenant '$tenantIdentifier'." }
        } catch (e: SQLException) {
            log.error(e) { "Error closing connection for tenant '$tenantIdentifier'." }
            throw e
        }
    }

    /**
     * Indicates whether the provider supports aggressive release of connections.
     * Returning false is generally safer unless specifically needed.
     */
    override fun supportsAggressiveRelease(): Boolean {
        return false
    }

    override fun isUnwrappableAs(p0: Class<*>): Boolean {
        return false
    }

    override fun <T : Any?> unwrap(unwrapType: Class<T>): T {
        throw UnsupportedOperationException("TenantConnectionProvider does not support unwrap()")
    }
}