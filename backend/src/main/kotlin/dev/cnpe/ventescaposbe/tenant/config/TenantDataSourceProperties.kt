package dev.cnpe.ventescaposbe.tenant.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.validation.annotation.Validated

/**
 * Configuration properties for tenant data-sources.
 * Maps properties starting with 'tenant.datasource'/
 */
@ConfigurationProperties(prefix = "app.datasource.tenant")
@Validated
data class TenantDataSourceProperties(

    @field:NotBlank
    val urlTemplate: String, // jdbc:postgresql://{host}:{port}/ventesca_{tenant}_db

    @field:NotBlank
    val username: String,

    @field:NotBlank
    val password: String,

    @NestedConfigurationProperty
    val hikari: HikariTenantProperties = HikariTenantProperties()

)

data class HikariTenantProperties(
    @field:NotNull @field:Positive
    val maximumPoolSize: Int = 10,

    @field:NotBlank
    val poolNamePrefix: String = "tenant-pool-"
)
