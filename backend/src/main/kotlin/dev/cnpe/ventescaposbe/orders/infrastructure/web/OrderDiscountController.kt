package dev.cnpe.ventescaposbe.orders.infrastructure.web

import dev.cnpe.ventescaposbe.orders.application.dto.request.ApplyDiscountRequest
import dev.cnpe.ventescaposbe.orders.application.dto.response.OrderResponse
import dev.cnpe.ventescaposbe.orders.application.service.DiscountApplicationService
import dev.cnpe.ventescaposbe.shared.application.dto.ApiResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Manage discount application for orders")
class OrderDiscountController(
    private val discountApplicationService: DiscountApplicationService
) {

    @PostMapping("/{orderId}/items/{itemId}/discount")
    @Operation(summary = "Apply a discount rule to a specific order item")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Discount applied successfully",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input or discount rule not applicable",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Order, Item, or DiscountRule not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Order not pending or discount type mismatch",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun applyItemDiscount(
        @Parameter(description = "ID of the order") @PathVariable orderId: Long,
        @Parameter(description = "ID of the order item") @PathVariable itemId: Long,
        @Valid @RequestBody request: ApplyDiscountRequest
    ): ResponseEntity<OrderResponse> {
        val updatedOrder = discountApplicationService.applyItemDiscount(orderId, itemId, request)
        return ResponseEntity.ok(updatedOrder)
    }

    @DeleteMapping("/{orderId}/items/{itemId}/discount")
    @Operation(summary = "Remove discount from a specific order item")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Discount removed successfully",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Order or Item not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Order not pending",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun removeItemDiscount(
        @Parameter(description = "ID of the order") @PathVariable orderId: Long,
        @Parameter(description = "ID of the order item") @PathVariable itemId: Long
    ): ResponseEntity<OrderResponse> {
        val updatedOrder = discountApplicationService.removeItemDiscount(orderId, itemId)
        return ResponseEntity.ok(updatedOrder)
    }


    @PostMapping("/{orderId}/discount")
    @Operation(summary = "Apply a discount rule to the entire order")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Discount applied successfully",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input or discount rule not applicable",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Order or DiscountRule not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Order not pending, discount type mismatch, or order discount already applied",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun applyOrderDiscount(
        @Parameter(description = "ID of the order") @PathVariable orderId: Long,
        @Valid @RequestBody request: ApplyDiscountRequest
    ): ResponseEntity<OrderResponse> {
        val updatedOrder = discountApplicationService.applyOrderDiscount(orderId, request)
        return ResponseEntity.ok(updatedOrder)
    }


    @DeleteMapping("/{orderId}/discount")
    @Operation(summary = "Remove order-level discount from the order")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Discount removed successfully",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Order not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Order not pending",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun removeOrderDiscount(
        @Parameter(description = "ID of the order") @PathVariable orderId: Long
    ): ResponseEntity<OrderResponse> {
        val updatedOrder = discountApplicationService.removeOrderDiscount(orderId)
        return ResponseEntity.ok(updatedOrder)
    }
}