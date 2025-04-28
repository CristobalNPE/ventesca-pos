package dev.cnpe.ventescaposbe.business.infrastructure.web

import dev.cnpe.ventescaposbe.business.application.dto.request.CreateBusinessBranchRequest
import dev.cnpe.ventescaposbe.business.application.dto.request.UpdateBusinessBasicsRequest
import dev.cnpe.ventescaposbe.business.application.dto.request.UpdateBusinessConfigurationRequest
import dev.cnpe.ventescaposbe.business.application.dto.request.UpdateBusinessContactInfoRequest
import dev.cnpe.ventescaposbe.business.application.dto.response.BusinessBranchInfo
import dev.cnpe.ventescaposbe.business.application.dto.response.BusinessDetailedResponse
import dev.cnpe.ventescaposbe.business.application.dto.response.BusinessStatusResponse
import dev.cnpe.ventescaposbe.business.application.service.BusinessManagementService
import dev.cnpe.ventescaposbe.shared.application.dto.ApiResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/business")
@Tag(
    name = "Business Management",
    description = "Endpoints for a Business Admin to manage their own business details, configuration, and branches."
)
class BusinessManagementController(
    private val businessManagementService: BusinessManagementService
) {

    @GetMapping("/details")
    @Operation(
        summary = "Get current business details",
        description = "Retrieves the complete details of the business associated with the current authenticated Business Admin."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Business details retrieved successfully.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BusinessDetailedResponse::class)
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden (User is not a BUSINESS_ADMIN or tenant resolution failed)."
            ),
            ApiResponse(
                responseCode = "404",
                description = "Business or BusinessUser link not found for authenticated user.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun getCurrentBusinessDetails(): BusinessDetailedResponse {
        return businessManagementService.getCurrentUserBusinessData()
    }

    @GetMapping("/status")
    @Operation(
        summary = "Get current business status",
        description = "Retrieves the operational status of the business for the current user."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Status retrieved.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BusinessStatusResponse::class)
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden (Tenant resolution failed).")
        ]
    )
    fun getCurrentBusinessStatus(): BusinessStatusResponse {
        return businessManagementService.getCurrentBusinessStatus()
    }

    @PutMapping("/basics")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Update basic business details",
        description = "Updates the business name and/or brand message."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Basic details updated successfully."),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden."),
            ApiResponse(
                responseCode = "404",
                description = "Business not found.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateBasics(@Valid @RequestBody request: UpdateBusinessBasicsRequest) {
        businessManagementService.updateBasics(request)
    }

    @PutMapping("/contact-info")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Update business contact information",
        description = "Updates the primary phone, email, and website for the business."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Contact info updated successfully."),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden."),
            ApiResponse(
                responseCode = "404",
                description = "Business not found.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateContactInfo(@Valid @RequestBody request: UpdateBusinessContactInfoRequest) {
        businessManagementService.updateContactInfo(request)
    }

    @PutMapping("/configuration")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Update business configuration",
        description = "Updates the default currency, tax percentage, and accepted payment methods."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Configuration updated successfully."),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden."),
            ApiResponse(
                responseCode = "404",
                description = "Business not found.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateConfiguration(@Valid @RequestBody request: UpdateBusinessConfigurationRequest) {
        businessManagementService.updateBusinessConfiguration(request)
    }

    @PostMapping("/branches")
    @Operation(
        summary = "Register a new branch",
        description = "Creates a new branch associated with the current business."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Branch created successfully.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BusinessBranchInfo::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden."),
            ApiResponse(
                responseCode = "404",
                description = "Business not found.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
            // TODO: Add 405 if branch limit is reached (needs check in service)
        ]
    )
    fun registerBranch(@Valid @RequestBody request: CreateBusinessBranchRequest): ResponseEntity<BusinessBranchInfo> {
        val createdBranch = businessManagementService.registerBranch(request)
        val location =
            URI.create("/business/branches/${createdBranch.branchId}")  //TODO: Should we have a top-level get for returning a branch by id?
        return ResponseEntity.created(location).body(createdBranch)
    }

    @PutMapping("/branches/{branchId}/_actions/set-main")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Set a branch as the main branch",
        description = "Designates the specified branch as the main branch for the business."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Main branch set successfully."),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden."),
            ApiResponse(
                responseCode = "404",
                description = "Business or Branch not found.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun setMainBranch(
        @Parameter(description = "ID of the branch to set as main") @PathVariable branchId: Long
    ) {
        businessManagementService.setMainBranch(branchId)
    }

    // TODO: Add endpoint for GET /business/branches (List branches for the business)
    // TODO: Add endpoint for DELETE /business/branches/{branchId}

}