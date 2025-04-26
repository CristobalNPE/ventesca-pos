package dev.cnpe.ventescabekotlin.config.jpa

import dev.cnpe.ventescabekotlin.business.infrastructure.persistence.BusinessRepository
import dev.cnpe.ventescabekotlin.currency.infrastructure.persistence.CurrencyRepository
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackageClasses = [
        BusinessRepository::class,
        CurrencyRepository::class
    ],
    entityManagerFactoryRef = "masterEntityManagerFactory",
    transactionManagerRef = "masterTransactionManager"
)
class MasterJpaConfig : BaseJpaConfig() {

    @Value("\${app.hibernate.debug:false}")
    private val showSql: Boolean = false


    /**
     * Configures the EntityManagerFactory for the master database.
     */
    @Bean("masterEntityManagerFactory")
    fun masterEntityManagerFactory(
        @Qualifier("masterDataSource") masterDataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {

        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = masterDataSource

        em.setPackagesToScan(
            "dev.cnpe.ventescabekotlin.business.domain",
            "dev.cnpe.ventescabekotlin.currency.domain"
        )

        em.jpaVendorAdapter = createHibernateAdapter(showSql)
        val properties = getBaseHibernateProperties(showSql)

        em.setJpaPropertyMap(properties)

        return em
    }

    /**
     * Configures the TransactionManager for the master database.
     */
    @Bean("masterTransactionManager")
    fun masterTransactionManager(
        @Qualifier("masterEntityManagerFactory") entityManagerFactory: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }

    @Bean("masterTransactionTemplate")
    fun masterTransactionTemplate(
        @Qualifier("masterTransactionManager") transactionManager: PlatformTransactionManager
    ): TransactionTemplate {
        return TransactionTemplate(transactionManager)
    }

}