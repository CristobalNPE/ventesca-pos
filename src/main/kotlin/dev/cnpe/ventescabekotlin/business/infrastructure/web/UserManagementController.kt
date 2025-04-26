package dev.cnpe.ventescabekotlin.business.infrastructure.web

import dev.cnpe.ventescabekotlin.business.application.dto.request.CreateBusinessUserRequest
import dev.cnpe.ventescabekotlin.business.application.service.UserManagementService
import dev.cnpe.ventescabekotlin.security.ports.dto.UserIdentity
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/business/users")
@Tag(
    name = "Business User Management",
    description = "Endpoints for Business Admins to manage users within their business."
)
@SecurityRequirement(name = "bearerAuth")
// TODO: Secure all endpoints with @PreAuthorize("hasRole('BUSINESS_ADMIN')")
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
                responseCode = "201",
                description = "User created successfully.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserIdentity::class))]
            ),
        ]
    //TODO: Add other api responses
    )
    // TODO: Add @PreAuthorize("hasRole('BUSINESS_ADMIN')")
    fun createBusinessUser(
        @Valid @RequestBody request: CreateBusinessUserRequest
    ): ResponseEntity<UserIdentity> {
        val createdUser = userManagementService.createBusinessUser(request.userData, request.roles)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    // TODO: Implement endpoints for listing users in business, updating, deleting, assigning roles
    // GET /business/users
    // PUT /business/users/{userIdpId}/roles
    // DELETE /business/users/{userIdpId}

}