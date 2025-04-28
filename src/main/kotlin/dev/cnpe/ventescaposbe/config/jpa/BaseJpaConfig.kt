package dev.cnpe.ventescaposbe.config.jpa

import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter

/**
 * Base configuration class providing common helper methods for JPA setup.
 */
abstract class BaseJpaConfig {

    companion object {
        private const val HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto"
        private const val HIBERNATE_SHOW_SQL = "hibernate.show_sql"
        private const val HIBERNATE_FORMAT_SQL = "hibernate.format_sql"
        private const val HIBERNATE_NAMING_STRATEGY = "hibernate.physical_naming_strategy"
        private const val CAMEL_CASE_TO_UNDERSCORES_STRATEGY =
            "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
        private const val HBM2DDL_NONE = "none"
//        private const val POSTGRESQL_DIALECT = "org.hibernate.dialect.PostgreSQLDialect"
    }


    /**
     * Creates a basic HibernateJpaVendorAdapter configured for PostgreSQL.
     *
     * @param showSql Whether to enable logging of generated SQL statements.
     * @return A configured HibernateJpaVendorAdapter instance.
     */
    protected fun createHibernateAdapter(showSql: Boolean): HibernateJpaVendorAdapter {
        val adapter = HibernateJpaVendorAdapter()

        // using the old java bean approach/ do not change
        adapter.setDatabase(Database.POSTGRESQL)
        adapter.setShowSql(showSql)
        return adapter
    }

    /**
     * Creates a base map of common Hibernate properties.
     *
     * @param showSql Whether to enable SQL logging properties.
     * @return A mutable map containing base Hibernate properties.
     */
    protected fun getBaseHibernateProperties(showSql: Boolean): MutableMap<String, Any> {
        return mutableMapOf(
            HIBERNATE_HBM2DDL_AUTO to HBM2DDL_NONE,
            // HIBERNATE_DIALECT to POSTGRESQL_DIALECT,
            HIBERNATE_SHOW_SQL to showSql,
            HIBERNATE_FORMAT_SQL to showSql,
            HIBERNATE_NAMING_STRATEGY to CAMEL_CASE_TO_UNDERSCORES_STRATEGY
        )
    }
}