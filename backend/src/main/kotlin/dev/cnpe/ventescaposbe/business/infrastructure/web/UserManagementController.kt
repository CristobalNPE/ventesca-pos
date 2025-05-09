package dev.cnpe.ventescaposbe.business.infrastructure.web

import dev.cnpe.ventescaposbe.business.application.dto.request.CreateBusinessUserRequest
import dev.cnpe.ventescaposbe.business.application.dto.response.BusinessUserInfo
import dev.cnpe.ventescaposbe.business.application.service.UserManagementService
import dev.cnpe.ventescaposbe.security.annotation.RequireAdmin
import dev.cnpe.ventescaposbe.security.ports.dto.UserIdentity
import dev.cnpe.ventescaposbe.shared.application.dto.ApiResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/business/users")
@Tag(
    name = "Business User Management",
    description = "Endpoints for Business Admins to manage users within their business."
)
@RequireAdmin
class UserManagementController(
    private val userManagementService: UserManagementService
) {

    @PostMapping
    @Operation(
        summary = "Create a new user for the current business",
        description = "Creates a Seller or Branch Manager user associated with the admin's business."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "User created successfully.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserIdentity::class)
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data (validation errors).",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized."
            ),
            ApiResponse(
                responseCode = "403", description = "Forbidden (User does not have BUSINESS_ADMIN role)."
            ),
            ApiResponse(
                responseCode = "409", description = "Conflict (e.g., duplicate email).",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "405",
                description = "Operation Not Allowed (e.g., User limit reached, invalid role assignment).",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun createBusinessUser(
        @Valid @RequestBody request: CreateBusinessUserRequest
    ): ResponseEntity<UserIdentity> {
        val createdUser = userManagementService.createBusinessUser(request.userData, request.roles)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @GetMapping("/{userIdpId}/branches")
    @Operation(summary = "Get assigned branches for a user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Branch IDs retrieved successfully",
                content = [Content(schema = Schema(type = "array", implementation = Long::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun getUserBranchAssignments(
        @Parameter(description = "The IdP ID of the user") @PathVariable userIdpId: String
    ): ResponseEntity<Set<Long>> {
        val branchIds = userManagementService.getUserBranchAssignments(userIdpId)
        return ResponseEntity.ok(branchIds)
    }

    @PutMapping("/{userIdpId}/branches")
    @Operation(summary = "Set/Replace assigned branches for a user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Branch assignments updated successfully"),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid Branch IDs provided",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun assignBranchesToUser(
        @Parameter(description = "The IdP ID of the user") @PathVariable userIdpId: String,
        @Parameter(description = "Set of Branch IDs to assign (replaces existing)") @RequestBody branchIds: Set<Long>
    ) {
        userManagementService.assignBranchesToUser(userIdpId, branchIds)
    }


    @GetMapping
    @Operation(
        summary = "List all users for the current business",
        description = "Retrieves a list of users (Admins, Managers, Sellers) associated with the Business Admin's business."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "User list retrieved successfully.",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = BusinessUserInfo::class))
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden (User does not have BUSINESS_ADMIN role)."),
            ApiResponse(
                responseCode = "404",
                description = "Business not found for the current admin (should not happen if tenant resolution works).",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun listBusinessUsers(): List<BusinessUserInfo> {
        return userManagementService.listBusinessUsers()
    }

    @DeleteMapping("/{userIdpId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete a user from the current business",
        description = "Deletes a Seller or Branch Manager user associated with the admin's business from both the IdP and the application."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "User deleted successfully."),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden (User does not have BUSINESS_ADMIN role, or target user belongs to another tenant)."
            ),
            ApiResponse(
                responseCode = "404", description = "User not found (in IdP or local link).",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "500", description = "Internal Server Error (e.g., IdP communication failure).",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun deleteBusinessUser(
        @Parameter(description = "The IdP ID (sub claim) of the user to delete") @PathVariable userIdpId: String
    ) {
        userManagementService.deleteBusinessUser(userIdpId)
    }


    // TODO: Implement endpoints for  updating, assigning roles
    // PUT /business/users/{userIdpId}/roles


}