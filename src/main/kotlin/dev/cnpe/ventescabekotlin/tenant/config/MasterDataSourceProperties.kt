package dev.cnpe.ventescabekotlin.tenant.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "app.datasource.master")
@Validated
data class MasterDataSourceProperties(

    @field:NotBlank
    val url: String,

    @field:NotBlank
    val username: String,

    @field:NotBlank
    val password: String,

    @NestedConfigurationProperty
    val hikari: HikariMasterProperties = HikariMasterProperties()

)

data class HikariMasterProperties(
    val maximumPoolSize: Int = 5,
    val poolName: String = "master-pool"
)