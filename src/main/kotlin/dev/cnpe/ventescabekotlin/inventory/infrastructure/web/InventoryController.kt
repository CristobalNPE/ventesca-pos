package dev.cnpe.ventescabekotlin.inventory.infrastructure.web

import dev.cnpe.ventescabekotlin.inventory.application.api.InventoryInfoPort
import dev.cnpe.ventescabekotlin.inventory.application.api.dto.BranchInventoryDetails
import dev.cnpe.ventescabekotlin.inventory.application.dto.request.UpdateStockRequest
import dev.cnpe.ventescabekotlin.inventory.application.service.InventoryManagementService
import dev.cnpe.ventescabekotlin.shared.application.dto.ApiResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/inventory")
@Tag(name = "Inventory", description = "Manage product stock levels per branch.")
class InventoryController(
    private val inventoryManagementService: InventoryManagementService,
    private val inventoryInfoPort: InventoryInfoPort
) {


    @GetMapping("/{productId}/branch/{branchId}")
    @Operation(
        summary = "Get inventory details for a product in a specific branch",
        description = "Retrieves current stock quantity, minimum levels, and status for a product within a single specified branch."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Inventory details retrieved successfully.",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = BranchInventoryDetails::class)
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "Inventory item not found for the given product and branch.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
            //TODO  Add 401, 403
        ]
    )
    fun getInventoryDetailsForProductInBranch(
        @Parameter(description = "ID of the product") @PathVariable productId: Long,
        @Parameter(description = "ID of the branch") @PathVariable branchId: Long
    ): BranchInventoryDetails {
        return inventoryInfoPort.getBranchInventoryDetails(productId, branchId)
    }

    @PutMapping("/{productId}/stock")
    @Operation(
        summary = "Update stock for a product in a specific branch",
        description = "Sets the absolute stock quantity and minimum level for a product in the branch specified within the request body. Records the reason for the change."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Stock updated successfully."),
            ApiResponse(
                responseCode = "400", description = "Invalid input data (e.g., missing fields, negative values).",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Inventory item not found for the given product and branch.",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
            // TODO: Add 401, 403
        ]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateStock(
        @Parameter(description = "ID of the product") @PathVariable productId: Long,
        @Valid @RequestBody request: UpdateStockRequest
    ) {
        inventoryManagementService.updateStock(productId, request)
    }

}