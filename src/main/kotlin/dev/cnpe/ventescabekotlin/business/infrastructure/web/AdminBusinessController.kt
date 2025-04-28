package dev.cnpe.ventescabekotlin.business.infrastructure.web

import dev.cnpe.ventescabekotlin.business.application.dto.request.AdminCreateBusinessRequest
import dev.cnpe.ventescabekotlin.business.application.dto.response.BusinessDetailedResponse
import dev.cnpe.ventescabekotlin.business.application.service.AdminBusinessService
import dev.cnpe.ventescabekotlin.shared.application.dto.ApiResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/admin/businesses")
@Tag(name = "Business Administration", description = "Endpoints for Superusers to manage businesses.")
class AdminBusinessController(
    private val adminBusinessService: AdminBusinessService
) {

    @PostMapping
    @Operation(
        summary = "Register a new Business and its Admin User",
        description = """
            Creates a new Business entity, its associated Tenant database and schema,
            creates the initial Business Admin user in the Identity Provider (e.g., Keycloak),
            and links the user to the business. Requires SUPERADMIN role.
            """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Business and Admin User created successfully.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BusinessDetailedResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data (validation failure).",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized (Not authenticated)."
            ),
            ApiResponse(
                responseCode = "403", description = "Forbidden (User does not have SUPERADMIN role)."
            ),
            ApiResponse(
                responseCode = "409", description = "Conflict (e.g., duplicate email or business name).",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal Server Error (e.g., IdP communication failure, tenant creation failure).",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    //TODO: Add @PreAuthorize("hasRole('SUPERADMIN')") below
    fun registerBusiness(
        @Valid @RequestBody request: AdminCreateBusinessRequest
    ): ResponseEntity<BusinessDetailedResponse> {
        val createdBusiness = adminBusinessService.registerNewBusinessAndAdmin(request)
        val location = URI.create("/admin/business/${createdBusiness.id}")
        return ResponseEntity.created(location).body(createdBusiness)
    }

    // TODO --- Add other Superuser endpoints later ---
    // GET /admin/businesses (List all)
    // GET /admin/businesses/{id} (Get specific by ID)
    // PUT /admin/businesses/{id}/status (Activate/Deactivate)

}