package dev.cnpe.ventescaposbe.tenant.config

import com.zaxxer.hikari.HikariDataSource
import dev.cnpe.ventescaposbe.tenant.DynamicTenantAwareRoutingSource
import dev.cnpe.ventescaposbe.tenant.TenantDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

@Configuration
class DataSourceConfig {


    /**
     * Creates the primary DataSource bean for the Master database.
     * Reads connection details directly from @Value annotations.
     */
    @Bean("masterDataSource")
    fun masterDataSource(properties: MasterDataSourceProperties): DataSource {
        log.info { "Creating Master DataSource bean (Pool: ${properties.hikari.poolName})..." }
        return HikariDataSource().apply {
            jdbcUrl = properties.url
            username = properties.username
            password = properties.password
            poolName = properties.hikari.poolName
            maximumPoolSize = properties.hikari.maximumPoolSize
            driverClassName = "org.postgresql.Driver" // Should we externalize this?
        }
    }

    /**
     * Creates the routing DataSource bean that directs connections
     * to the appropriate tenant DataSource based on TenantContext.
     * This will be the primary DataSource used by the application's EntityManagerFactory.
     */
    @Primary
    @Bean("tenantRoutingDataSource")
    fun tenantRoutingDataSource(
        @Qualifier("masterDataSource") masterDataSource: DataSource,
        tenantDataSource: TenantDataSource
    ): DataSource {
        log.info { "Creating Tenant Routing DataSource bean..." }

        val routingSource = DynamicTenantAwareRoutingSource(tenantDataSource)
        routingSource.setDefaultTargetDataSource(masterDataSource)

        val initialDataSources = HashMap<Any, Any>()
        initialDataSources["default"] = masterDataSource
        routingSource.setTargetDataSources(initialDataSources)

        routingSource.afterPropertiesSet()

        log.info { "Tenant Routing DataSource created. Default target: masterDataSource" }
        return routingSource
    }

}