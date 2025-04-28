package dev.cnpe.ventescaposbe

import dev.cnpe.ventescaposbe.business.config.BusinessLimitProperties
import dev.cnpe.ventescaposbe.security.config.KeycloakAdminProperties
import dev.cnpe.ventescaposbe.tenant.config.MasterDataSourceProperties
import dev.cnpe.ventescaposbe.tenant.config.TenantDataSourceProperties
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
