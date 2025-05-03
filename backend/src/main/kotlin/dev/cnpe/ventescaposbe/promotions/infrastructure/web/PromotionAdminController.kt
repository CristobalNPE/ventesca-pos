package dev.cnpe.ventescaposbe.promotions.infrastructure.web

import dev.cnpe.ventescaposbe.promotions.application.dto.request.CreateDiscountRuleRequest
import dev.cnpe.ventescaposbe.promotions.application.dto.request.UpdateDiscountRuleStatusRequest
import dev.cnpe.ventescaposbe.promotions.application.dto.response.DiscountRuleResponse
import dev.cnpe.ventescaposbe.promotions.application.service.PromotionAdminService
import dev.cnpe.ventescaposbe.security.annotation.RequireAdmin
import dev.cnpe.ventescaposbe.shared.application.dto.ApiResult
import dev.cnpe.ventescaposbe.shared.application.dto.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/admin/promotions/rules")
@Tag(name = "Promotion Management", description = "Endpoints for Admins to manage discount/promotion rules.")
@RequireAdmin//TODO is this ok? Check when building front
class PromotionAdminController(
    private val promotionAdminService: PromotionAdminService
) {

    @PostMapping
    @Operation(summary = "Create a new discount/promotion rule")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Discount rule created successfully",
                content = [Content(schema = Schema(implementation = DiscountRuleResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Duplicate rule name",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (Insufficient permissions)")
        ]
    )
    fun createDiscountRule(
        @Valid @RequestBody request: CreateDiscountRuleRequest
    ): ResponseEntity<DiscountRuleResponse> {
        val createdRule = promotionAdminService.createDiscountRule(request)
        val location = URI.create("/admin/promotions/rules/${createdRule.id}")
        return ResponseEntity.created(location).body(createdRule)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get discount rule details by ID")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Discount rule found",
                content = [Content(schema = Schema(implementation = DiscountRuleResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Discount rule not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun getDiscountRule(
        @Parameter(description = "ID of the discount rule to retrieve") @PathVariable id: Long
    ): DiscountRuleResponse {
        return promotionAdminService.getDiscountRule(id)
    }

    @GetMapping
    @Operation(summary = "List all discount/promotion rules (paginated)")
    @PageableAsQueryParam
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "List retrieved successfully",
                content = [Content(schema = Schema(implementation = PageResponse::class))] // TODO: Adjust schema if failing to represent PageResponse<DiscountRuleResponse>
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun listDiscountRules(
        @Parameter(hidden = true) @PageableDefault(size = 20, sort = ["name"]) pageable: Pageable
    ): PageResponse<DiscountRuleResponse> {
        return promotionAdminService.listDiscountRules(pageable)
    }

    @PutMapping("/{id}/status")
    @RequireAdmin
    @Operation(summary = "Activate or deactivate a discount rule")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Status updated successfully",
                content = [Content(schema = Schema(implementation = DiscountRuleResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid request body",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Discount rule not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun updateDiscountRuleStatus(
        @Parameter(description = "ID of the discount rule") @PathVariable id: Long,
        @Valid @RequestBody request: UpdateDiscountRuleStatusRequest
    ): DiscountRuleResponse {
        return promotionAdminService.updateDiscountRuleStatus(id, request.isActive)

    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "Delete a discount rule")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Discount rule deleted successfully"),
            ApiResponse(
                responseCode = "404", description = "Discount rule not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDiscountRule(
        @Parameter(description = "ID of the discount rule to delete") @PathVariable id: Long
    ) {
        promotionAdminService.deleteDiscountRule(id)
    }

    // TODO: Add PUT or PATCH endpoint for full updates later
    // @PutMapping("/{id}")
    // fun updateDiscountRule(...)

}