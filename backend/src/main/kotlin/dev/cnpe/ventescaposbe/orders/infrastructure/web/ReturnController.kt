package dev.cnpe.ventescaposbe.orders.infrastructure.web

import dev.cnpe.ventescaposbe.orders.application.dto.request.ProcessReturnRequest
import dev.cnpe.ventescaposbe.orders.application.dto.response.ReturnTransactionResponse
import dev.cnpe.ventescaposbe.orders.application.dto.response.ReturnableItemInfo
import dev.cnpe.ventescaposbe.orders.application.service.ReturnService
import dev.cnpe.ventescaposbe.security.annotation.RequirePosOperatorRoles
import dev.cnpe.ventescaposbe.shared.application.dto.ApiResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
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
@RequestMapping("/returns")
@Tag(name = "Returns", description = "Process product returns and refunds")
@RequirePosOperatorRoles
class ReturnController(
    private val returnService: ReturnService
) {


    @GetMapping("/orders/{orderId}/returnable-items")
    @Operation(
        summary = "Get items eligible for return from a completed order",
        description = "Retrieves a list of items from a specified completed order that can still be returned (quantity > returnedQuantity)."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "List of returnable items retrieved successfully.",
                content = [Content(array = ArraySchema(schema = Schema(implementation = ReturnableItemInfo::class)))]
            ),
            ApiResponse(
                responseCode = "404", description = "Original order not found.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Order is not in a COMPLETED or REFUNDED state.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden.")
        ]
    )
    fun getReturnableItems(
        @Parameter(description = "ID of the original completed order") @PathVariable orderId: Long
    ): List<ReturnableItemInfo> {
        return returnService.getReturnableItems(orderId)
    }

    @PostMapping
    @Operation(
        summary = "Process a new return transaction",
        description = "Processes a return for specified items from a completed order, records the transaction, and triggers inventory updates."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Return processed successfully.",
                content = [Content(schema = Schema(implementation = ReturnTransactionResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data (validation error, quantity mismatch, etc.).",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Original order or order item not found.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict / Invalid State (e.g., order not completed, trying to return more than possible).",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden.")
        ]
    )
    fun processReturn(
        @Valid @RequestBody request: ProcessReturnRequest
    ): ResponseEntity<ReturnTransactionResponse> {
        val processedReturn = returnService.processReturn(request)

        val location = URI.create("/returns/${processedReturn.id}")
        return ResponseEntity.created(location).body(processedReturn)
    }

    // TODO:
    // @GetMapping("/{returnId}")
    // fun getReturnDetails(@PathVariable returnId: Long): ReturnTransactionResponse
    //
    // @GetMapping
    // fun listReturns(@PageableDefault pageable: Pageable): PageResponse<ReturnSummaryResponse>

}