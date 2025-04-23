package dev.cnpe.ventescabekotlin

import dev.cnpe.ventescabekotlin.tenant.config.MasterDataSourceProperties
import dev.cnpe.ventescabekotlin.tenant.config.TenantDataSourceProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(TenantDataSourceProperties::class, MasterDataSourceProperties::class)
class VentescaBeKotlinApplication

fun main(args: Array<String>) {
    runApplication<VentescaBeKotlinApplication>(*args)
}
