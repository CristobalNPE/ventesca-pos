package dev.cnpe.ventescaposbe.config

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ActiveProfiles("test")
abstract class BaseIntegrationTest {

    companion object {

        @JvmStatic
        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer("postgres:16.8-alpine").apply {
            withDatabaseName("ventesca_test_db")
            withUsername("testuser")
            withPassword("testpass")
        }

        @JvmStatic
        @DynamicPropertySource
        fun databaseProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)

            registry.add("spring.liquibase.url", postgresContainer::getJdbcUrl)
            registry.add("spring.liquibase.user", postgresContainer::getUsername)
            registry.add("spring.liquibase.password", postgresContainer::getPassword)

            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") { "https://test-issuer.ventesca.dev/realms/test" }
            registry.add("keycloak.admin.server-url") { "http://dummy-keycloak:8080" }
            registry.add("keycloak.admin.realm") { "test-realm" }
            registry.add("keycloak.admin.client-id") { "test-admin-cli" }
            registry.add("keycloak.admin.client-secret") { "test-secret" }
            registry.add("keycloak.admin.grant-type") { "client_credentials" }
        }
    }

}