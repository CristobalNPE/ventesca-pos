package dev.cnpe.ventescaposbe.orders.infrastructure.web

import dev.cnpe.ventescaposbe.orders.application.dto.request.AddItemToOrderRequest
import dev.cnpe.ventescaposbe.orders.application.dto.request.AddPaymentRequest
import dev.cnpe.ventescaposbe.orders.application.dto.request.UpdateOrderItemQuantityRequest
import dev.cnpe.ventescaposbe.orders.application.dto.response.OrderResponse
import dev.cnpe.ventescaposbe.orders.application.service.OrderService
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
import java.net.URI

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Manage sales orders.")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping("/{branchId}")
    @Operation(summary = "Start a new empty order")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "New order created successfully",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input (e.g., missing branchId)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Forbidden (User cannot create order for this branch)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun startNewOrder(
        @PathVariable branchId: Long
    ): ResponseEntity<OrderResponse> {
        val createdOrder = orderService.startNewOrder(branchId)
        return ResponseEntity
            .created(URI.create("/orders/${createdOrder.id}"))
            .body(createdOrder)
    }

    @PostMapping("/{orderId}/items")
    @Operation(summary = "Add an item to a pending order")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Item added successfully, returns updated order",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input (validation error)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Order or Product not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict / Invalid State (e.g., order not pending, insufficient stock)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun addItemToOrder(
        @Parameter(description = "ID of the pending order") @PathVariable orderId: Long,
        @Valid @RequestBody request: AddItemToOrderRequest
    ): ResponseEntity<OrderResponse> {
        val updatedOrder = orderService.addItem(orderId, request)
        return ResponseEntity.ok(updatedOrder)
    }

    @PutMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Update the quantity of an item in a pending order")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Item quantity updated successfully, returns updated order",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input (e.g., non-positive quantity)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Order or OrderItem not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Conflict / Invalid State (e.g., order not pending)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateOrderItemQuantity(
        @Parameter(description = "ID of the pending order") @PathVariable orderId: Long,
        @Parameter(description = "ID of the order item to update") @PathVariable itemId: Long,
        @Valid @RequestBody request: UpdateOrderItemQuantityRequest
    ): OrderResponse {
        return orderService.updateItemQuantity(orderId, itemId, request)
    }


    @DeleteMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Remove an item from a pending order")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Item removed successfully, returns updated order",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Order or OrderItem not found in the specified order",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Conflict / Invalid State (e.g., order not pending)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun removeOrderItem(
        @Parameter(description = "ID of the pending order") @PathVariable orderId: Long,
        @Parameter(description = "ID of the order item to remove") @PathVariable itemId: Long
    ): OrderResponse {
        return orderService.removeItem(orderId, itemId)
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get detailed information for a specific order")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Order details retrieved successfully",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Order not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun getOrderById(
        @Parameter(description = "ID of the order to retrieve") @PathVariable orderId: Long
    ): OrderResponse {
        return orderService.getOrderDetails(orderId)
    }

    @PostMapping("/{orderId}/payments")
    @Operation(summary = "Add a payment to an order")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Payment added successfully, returns updated order",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input (validation error, overpayment)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Order not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Conflict / Invalid State (e.g., order not pending or processing)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun addPaymentToOrder(
        @Parameter(description = "ID of the order") @PathVariable orderId: Long,
        @Valid @RequestBody request: AddPaymentRequest
    ): OrderResponse {
        return orderService.addPayment(orderId, request)
    }


    @PostMapping("/{orderId}/complete")
    @Operation(summary = "Complete a fully paid order")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Order completed successfully, returns final order state",
                content = [Content(schema = Schema(implementation = OrderResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Order not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict / Invalid State (e.g., order not processing or not fully paid)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun completeOrder(
        @Parameter(description = "ID of the order to complete") @PathVariable orderId: Long
    ): OrderResponse {
        return orderService.completeOrder(orderId)
    }

}