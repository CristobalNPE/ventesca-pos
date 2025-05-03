package dev.cnpe.ventescaposbe.tenant.infrastructure.web

import dev.cnpe.ventescaposbe.security.annotation.RequireSuperuser
import dev.cnpe.ventescaposbe.tenant.dto.TenantOperationResult
import dev.cnpe.ventescaposbe.tenant.service.TenantManagementService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Duration
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

//TODO: MAKE ASYNC SOME DAY

@RestController
@RequestMapping("/admin/tenants")
@Tag(name = "Tenant Administration", description = "Endpoints for managing tenants (Requires SUPERADMIN role).")
@RequireSuperuser
class TenantAdminController(
    private val tenantManagementService: TenantManagementService
) {

    @PostMapping("/_actions/run-all-schema-updates")
    @Operation(
        summary = "Trigger schema migrations for all tenants",
        description = """
            Manually triggers the execution of Liquibase schema updates against ALL existing tenant databases.
            This is intended for controlled production deployments during maintenance windows.
            WARNING: This operation can take a significant amount of time depending on the number of tenants.
            The request will block until all updates are attempted. Check server logs for detailed progress and errors.
            Requires SUPERADMIN role.
            """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Schema update process completed (check logs for details).",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TenantOperationResult::class)
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden (User does not have SUPERADMIN role)."),
            ApiResponse(responseCode = "500", description = "Internal error during the update process.")
        ]
    )
    fun runAllTenantSchemaUpdates(): ResponseEntity<TenantOperationResult> {
        log.warn { "🔸 Received request to run schema updates for ALL tenants. This may take time..." }
        var resultMessage = "Schema update process finished."
        var status = HttpStatus.OK
        var durationMillis: Long = 0

        try {
            durationMillis = measureTimeMillis {
                tenantManagementService.updateAllTenantsSchema()
            }
            log.info { "Finished processing run-all-schema-updates request in ${Duration.ofMillis(durationMillis)}." }
        } catch (e: Exception) {
            log.error(e) { "Critical error during run-all-schema-updates endpoint execution!" }
            resultMessage = "Error occurred during schema update process initiation. Check server logs."
            status = HttpStatus.INTERNAL_SERVER_ERROR
        }

        val result = TenantOperationResult(resultMessage, durationMillis)
        return ResponseEntity(result, status)
    }

    @PostMapping("/{tenantId}/_actions/run-schema-update")
    @Operation(
        summary = "Trigger schema migration for a single tenant",
        description = "Updates schema for a single tenant. Requires SUPERADMIN role. Check server logs for detailed progress and errors."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Schema update process completed (check logs for details).",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TenantOperationResult::class)
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden (User does not have SUPERADMIN role)."),
            ApiResponse(responseCode = "500", description = "Internal error during the update process.")
        ]
    )
    fun runSchemaUpdatesForSingleTenant(
        @PathVariable(name = "tenantId") tenantId: String
    )
            : ResponseEntity<TenantOperationResult> {
        var resultMessage = "Schema update process finished."
        var status = HttpStatus.OK
        var durationMillis: Long = 0

        try {
            durationMillis = measureTimeMillis {
                tenantManagementService.updateTenantSchema(tenantId)
            }
            log.info { "Finished updating schema for tenant $tenantId in ${Duration.ofMillis(durationMillis)}." }
        } catch (e: Exception) {
            log.error { "Critical error during run-schema-updates endpoint execution for tenant $tenantId!" }
            resultMessage = "Error occurred during schema update process initiation. Check server logs."
            status = HttpStatus.INTERNAL_SERVER_ERROR
        }
        val result = TenantOperationResult(resultMessage, durationMillis)
        return ResponseEntity(result, status)
    }

    @GetMapping("/_actions/list-tenants")
    @Operation(summary = "List all registered tenants", description = "Lists all registered tenants (requires SUPERADMIN role).")
    @ApiResponse(
        responseCode = "200",
        description = "List of tenant IDs retrieved successfully.",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = String::class, type = "array"),
//            array = ArraySchema(schema = Schema(type = "string", example = "mybusiness_a1b2c3"))
        )]
    )
    fun listAllRegisteredTenants(): Set<String> {
        return tenantManagementService.getTenantIds()
    }

}