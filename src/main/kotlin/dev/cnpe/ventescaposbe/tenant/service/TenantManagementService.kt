package dev.cnpe.ventescaposbe.tenant.service

import dev.cnpe.ventescaposbe.business.infrastructure.persistence.BusinessRepository
import dev.cnpe.ventescaposbe.tenant.TenantDataSource
import dev.cnpe.ventescaposbe.tenant.exception.TenantCreationException
import dev.cnpe.ventescaposbe.tenant.vo.TenantIdentifier
import io.github.oshai.kotlinlogging.KotlinLogging
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

@Service
class TenantManagementService(
    @Qualifier("masterDataSource") private val masterDataSource: DataSource,
    private val tenantDataSource: TenantDataSource,
    private val businessRepository: BusinessRepository
) {

    companion object {
        private const val TENANT_CHANGELOG_PATH = "db/changelog/tenant-changelog-master.xml"
        private const val TENANT_DB_NAME_PREFIX = "ventesca_"
        private const val TENANT_DB_NAME_SUFFIX = "_db"
    }

    /**
     * Creates a new tenant: generates ID, creates database, runs migrations, adds datasource pool.
     * IMPORTANT: Database creation happens outside main transaction.
     *
     * @param businessName Name used to generate the tenant identifier.
     * @return The generated TenantIdentifier.
     * @throws TenantCreationException if any step fails.
     */
    @Transactional(propagation = Propagation.NEVER)
    fun createTenant(businessName: String): TenantIdentifier {
        val tenantId = TenantIdentifier.generateFrom(businessName)
        val dbName = generateDatabaseName(tenantId)
        log.info { "Attempting to create tenant: ID=${tenantId.value}, DB=$dbName" }

        try {
            // 1. Create the physical database (No Spring Transaction)
            createTenantDatabase(dbName)

            // 2. Add DataSource pool to cache BEFORE running migrations
            // This allows Liquibase to get a connection via the TenantDataSource
            tenantDataSource.addTenant(tenantId.value)

            runLiquibaseUpdate(tenantId.value)

            log.info { "✅ Successfully created and initialized tenant: ${tenantId.value}" }
            return tenantId
        } catch (e: Exception) {
            log.error(e) { "❌ Tenant creation failed for ID: ${tenantId.value}. Initiating cleanup." }
            cleanupFailedTenantCreation(tenantId.value, dbName)
            throw TenantCreationException(
                tenantIdAttempted = tenantId.value,
                message = "Failed to create tenant '${tenantId.value}': ${e.message}",
                cause = e
            )
        }
    }

    /**
     * Creates the physical database using raw JDBC on the master DataSource.
     * WARNING: Requires appropriate DB user privileges. Runs outside Spring transactions.
     */
    private fun createTenantDatabase(dbName: String) {
        log.debug { "Executing CREATE DATABASE $dbName" }
        try {
            masterDataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("CREATE DATABASE \"$dbName\"")
                }
            }
            log.info { "Database '$dbName' created successfully." }
        } catch (e: SQLException) {
            // Check if error is because DB already exists
            // PSQLState for "duplicate_database" is "42P04"
            if (e.sqlState == "42P04") {
                log.warn { "Database '$dbName' already exists. Assuming schema setup is next step." }
            } else {
                log.error(e) { "SQL Exception during CREATE DATABASE $dbName (SQLState: ${e.sqlState})" }
                throw TenantCreationException(
                    message = "Failed to execute CREATE DATABASE statement for '$dbName'",
                    cause = e
                )
            }
        } catch (e: Exception) {
            log.error(e) { "Unexpected error during CREATE DATABASE $dbName" }
            throw TenantCreationException(message = "Failed creating database '$dbName'", cause = e)
        }
    }

    /**
     * Runs Liquibase schema migrations for the specified tenant ID.
     */
    @Throws(TenantCreationException::class)
    private fun runLiquibaseUpdate(tenantId: String) {
        log.info { "Running Liquibase migrations for tenant: $tenantId" }
        var connection: Connection? = null
        try {
            connection = DataSourceUtils.getConnection(tenantDataSource.getDataSource(tenantId))
            val jdbcConnection = JdbcConnection(connection)
            val database =
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)

            val liquibase = Liquibase(TENANT_CHANGELOG_PATH, ClassLoaderResourceAccessor(), database)
            liquibase.update()

            log.info { "Liquibase migrations completed successfully for tenant: $tenantId" }
        } catch (e: Exception) {
            log.error(e) { "Liquibase migration failed for tenant: $tenantId" }
            throw TenantCreationException(
                tenantIdAttempted = tenantId,
                message = "Liquibase migration failed for tenant '$tenantId': ${e.message}",
                cause = e
            )
        } finally {
            if (connection != null) {
                DataSourceUtils.releaseConnection(connection, tenantDataSource.getDataSource(tenantId))
                log.trace { "Released Liquibase connection for tenant: $tenantId" }
            }
        }
    }

    /**
     * Attempts to clean up resources if tenant creation fails.
     * Removes the DataSource pool and optionally tries to drop the database.
     */
    private fun cleanupFailedTenantCreation(tenantId: String, dbName: String) {
        log.warn { "Attempting cleanup for failed tenant creation: ID=$tenantId, DB=$dbName" }
        try {
            tenantDataSource.removeTenant(tenantId)
        } catch (e: Exception) {
            log.error(e) { "Cleanup failed: Error removing DataSource for tenant $tenantId" }
        }

        // This might fail if connections are still open or due to permissions.
        // Maybe should make DB drop a separate manual/admin operation?????
        try {
            log.warn { "Attempting to DROP DATABASE $dbName as part of cleanup." }
            masterDataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("DROP DATABASE IF EXISTS \"$dbName\"")
                }
            }
            log.info { "Database '$dbName' dropped during cleanup." }
        } catch (e: Exception) {
            log.error(e) { "Cleanup failed: Could not drop database $dbName. Manual cleanup may be required." }
        }
    }

    /**
     * Removes the tenant's DataSource pool from the cache. Does NOT drop the database.
     */
    fun removeTenantDataSource(tenantId: String) {
        tenantDataSource.removeTenant(tenantId)
    }

    /**
     * Runs Liquibase schema update on an existing tenant's database.
     */
    // Can run without Spring transaction as Liquibase manages its own
    @Transactional(propagation = Propagation.NEVER)
    fun updateTenantSchema(tenantId: String) {
        try {
            runLiquibaseUpdate(tenantId)
        } catch (e: TenantCreationException) {
            throw TenantCreationException(
                tenantIdAttempted = tenantId,
                message = "Failed to update tenant schema for '$tenantId': ${e.message}",
                cause = e.cause ?: e
            )
        }
    }

    /**
     * Retrieves all distinct tenant IDs stored in the master database's businesses table.
     * Requires BusinessRepository to be migrated and injected.
     */
    // Specify master transaction manager, read-only
    @Transactional(readOnly = true, transactionManager = "masterTransactionManager")
    fun getTenantIds(): Set<String> {
        log.debug { "Querying master database for all distinct tenant IDs..." }
        return businessRepository.findAllDistinctTenantIds()
    }

    /**
     * Runs Liquibase schema updates on all known tenants retrieved from the master database.
     */
    fun updateAllTenantsSchema() {
        val tenantIds = getTenantIds()
        if (tenantIds.isEmpty()) {
            log.info { "No tenant IDs found in master database. Skipping schema updates." }
            return
        }

        log.info { "Starting schema updates for ${tenantIds.size} tenants..." }
        var successCount = 0
        var failureCount = 0
        tenantIds.forEach { tenantId ->
            try {
                updateTenantSchema(tenantId)
                log.info { "✅ Successfully updated schema for tenant [$tenantId]" }
                successCount++
            } catch (e: Exception) {
                log.error(e) { "❌ Failed to update schema for tenant [$tenantId]" }
                failureCount++
            }
        }
        log.info { "Finished tenant schema updates. Success: $successCount, Failures: $failureCount" }
    }

    private fun generateDatabaseName(tenantId: TenantIdentifier): String {
        val safeTenantValue = tenantId.value.lowercase().replace(Regex("[^a-z0-9_]"), "") // ensure valid name for db
        return "$TENANT_DB_NAME_PREFIX${safeTenantValue}$TENANT_DB_NAME_SUFFIX"
    }
}