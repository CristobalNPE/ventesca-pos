package dev.cnpe.ventescaposbe.tenant.config

import dev.cnpe.ventescaposbe.tenant.service.TenantManagementService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

/**
 * Optional CommandLineRunner that triggers schema updates for all existing tenants
 * on application startup if the property 'app.tenant.schema.auto-update-on-startup' is set to true.
 */
@Component
@Order(1)
@ConditionalOnProperty(
    name = ["app.tenant.schema.auto-update-on-startup"],
    havingValue = "true",
    matchIfMissing = false // do not run if property missing
)
class TenantSchemaInitializer(
    private val tenantManagementService: TenantManagementService
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        log.info { "✨ Schema auto-update enabled. Starting tenant schema updates..." }
        val duration = measureTimeMillis {
            try {
                tenantManagementService.updateAllTenantsSchema()
            } catch (e: Exception) {
                log.error(e) { "Error during automatic tenant schema update! Application will continue starting, but tenant schemas might be inconsistent." }
            }
        }
        val formattedDuration = if (duration >= 1000) {
            String.format("%.2f seconds", duration / 1000.0)
        } else {
            "$duration ms"
        }
        log.info { "✨ Finished tenant schema updates in $formattedDuration" }
    }
}