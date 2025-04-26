package dev.cnpe.ventescabekotlin.config.jpa

import dev.cnpe.ventescabekotlin.brands.infrastructure.BrandRepository
import dev.cnpe.ventescabekotlin.business.infrastructure.persistence.BusinessRepository
import dev.cnpe.ventescabekotlin.categories.infrastructure.CategoryRepository
import dev.cnpe.ventescabekotlin.currency.infrastructure.persistence.CurrencyRepository
import dev.cnpe.ventescabekotlin.suppliers.infrastructure.persistence.SupplierRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManagerFactory
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.*
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

@Configuration
@EnableJpaRepositories(
    basePackages = [
        "org.springframework.modulith.events.jpa" // for modulith event publication repo
    ],
    basePackageClasses = [
        BrandRepository::class,
        CategoryRepository::class,
        SupplierRepository::class
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [
                BusinessRepository::class,
                CurrencyRepository::class
            ]
        )
    ],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
class TenantJpaConfig : BaseJpaConfig() {

    @Value("\${app.hibernate.debug:false}")
    private val showSql: Boolean = false

    companion object {
        // Constants for Hibernate multi-tenancy properties
        private const val HIBERNATE_MULTI_TENANCY = "hibernate.multiTenancy"
        private const val HIBERNATE_TENANT_RESOLVER = "hibernate.tenant_identifier_resolver"
        private const val HIBERNATE_CONNECTION_PROVIDER = "hibernate.multi_tenant_connection_provider"
        private const val MULTI_TENANCY_STRATEGY_DATABASE = "DATABASE"
    }

    /**
     * Configures the primary EntityManagerFactory for tenant databases.
     * This EMF is responsible for entities in modules like brands, categories, catalog, etc.
     */
    @Primary
    @Bean("entityManagerFactory")
    fun entityManagerFactory(
        dataSource: DataSource, // primary DS, it should be the routing one

        @Qualifier("multiTenantDbConnectionProvider")
        multiTenantConnectionProvider: MultiTenantConnectionProvider<String>,

        @Qualifier("tenantSchemaResolver")
        tenantIdentifierResolver: CurrentTenantIdentifierResolver<String>
    ): LocalContainerEntityManagerFactoryBean {
        log.info { "Configuring primary EntityManagerFactory for multi-tenancy..." }
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource

        em.setPackagesToScan(
            "dev.cnpe.ventescabekotlin.brands.domain",
            "dev.cnpe.ventescabekotlin.categories.domain",
            "dev.cnpe.ventescabekotlin.suppliers.domain",
            "dev.cnpe.ventescabekotlin.catalog.domain.model",
            "dev.cnpe.ventescabekotlin.inventory.domain",
            "dev.cnpe.ventescabekotlin.shared.domain",
            "org.springframework.modulith.events.jpa"
            // Add ALL packages containing entities managed by the tenant EMF
            // Ensure these DO NOT include master-only entities (like Business, Currency)
        )

        em.jpaVendorAdapter = createHibernateAdapter(showSql)

        val properties = getBaseHibernateProperties(showSql)
        properties[HIBERNATE_MULTI_TENANCY] = MULTI_TENANCY_STRATEGY_DATABASE
        properties[HIBERNATE_TENANT_RESOLVER] = tenantIdentifierResolver
        properties[HIBERNATE_CONNECTION_PROVIDER] = multiTenantConnectionProvider

        em.setJpaPropertyMap(properties)

        log.info { "Primary EntityManagerFactory configured with multi-tenancy strategy: DATABASE" }
        return em
    }

    /**
     * Configures the primary PlatformTransactionManager for tenant databases.
     */
    @Primary
    @Bean("transactionManager")
    fun transactionManager(
        entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}