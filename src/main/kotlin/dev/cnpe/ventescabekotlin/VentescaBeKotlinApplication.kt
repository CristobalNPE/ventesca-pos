package dev.cnpe.ventescabekotlin

import dev.cnpe.ventescabekotlin.business.config.BusinessLimitProperties
import dev.cnpe.ventescabekotlin.security.config.KeycloakAdminProperties
import dev.cnpe.ventescabekotlin.tenant.config.MasterDataSourceProperties
import dev.cnpe.ventescabekotlin.tenant.config.TenantDataSourceProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    TenantDataSourceProperties::class,
    MasterDataSourceProperties::class,
    KeycloakAdminProperties::class,
    BusinessLimitProperties::class
)
class VentescaBeKotlinApplication

fun main(args: Array<String>) {
    runApplication<VentescaBeKotlinApplication>(*args)
}
