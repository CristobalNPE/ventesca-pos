package dev.cnpe.ventescaposbe.config.jpa

import dev.cnpe.ventescaposbe.brands.infrastructure.persistence.BrandRepository
import dev.cnpe.ventescaposbe.business.infrastructure.persistence.BusinessRepository
import dev.cnpe.ventescaposbe.catalog.infrastructure.persistence.ProductPriceRepository
import dev.cnpe.ventescaposbe.catalog.infrastructure.persistence.ProductRepository
import dev.cnpe.ventescaposbe.categories.infrastructure.CategoryRepository
import dev.cnpe.ventescaposbe.currency.infrastructure.persistence.CurrencyRepository
import dev.cnpe.ventescaposbe.inventory.infrastructure.persistence.InventoryItemRepository
import dev.cnpe.ventescaposbe.suppliers.infrastructure.persistence.SupplierRepository
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
        SupplierRepository::class,
        ProductRepository::class,
        ProductPriceRepository::class,
        InventoryItemRepository::class
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
            "dev.cnpe.ventescaposbe.brands.domain",
            "dev.cnpe.ventescaposbe.categories.domain",
            "dev.cnpe.ventescaposbe.suppliers.domain",
            "dev.cnpe.ventescaposbe.catalog.domain.model",
            "dev.cnpe.ventescaposbe.inventory.domain",
            "dev.cnpe.ventescaposbe.shared.domain",
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