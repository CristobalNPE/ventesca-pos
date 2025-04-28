package dev.cnpe.ventescaposbe.suppliers.infrastructure.web

import dev.cnpe.ventescaposbe.shared.application.dto.ApiResult
import dev.cnpe.ventescaposbe.suppliers.application.dto.request.CreateSupplierRequest
import dev.cnpe.ventescaposbe.suppliers.application.dto.request.UpdateSupplierRequest
import dev.cnpe.ventescaposbe.suppliers.application.dto.request.UpdateSupplierStatusRequest
import dev.cnpe.ventescaposbe.suppliers.application.dto.response.SupplierDetailedResponse
import dev.cnpe.ventescaposbe.suppliers.application.dto.response.SupplierSummaryResponse
import dev.cnpe.ventescaposbe.suppliers.application.service.SupplierService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
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
@RequestMapping("/suppliers")
@Tag(name = "Suppliers", description = "Manage suppliers for the business.")
class SupplierController(
    private val supplierService: SupplierService
) {

    @PostMapping
    @Operation(summary = "Register a new supplier")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Supplier created successfully",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SupplierSummaryResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Duplicate supplier name",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun registerSupplier(@Valid @RequestBody request: CreateSupplierRequest): ResponseEntity<SupplierSummaryResponse> {
        val created = supplierService.registerSupplier(request)
        return ResponseEntity.created(URI.create("/suppliers/${created.id}")).body(created)
    }

    @GetMapping
    @Operation(summary = "Get all supplier summaries")
    @ApiResponse(
        responseCode = "200", description = "List of supplier summaries retrieved",
        content = [Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = SupplierSummaryResponse::class))
        )]
    )
    fun getAllSuppliers(): List<SupplierSummaryResponse> {
        return supplierService.getAllSuppliers()
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier details by ID")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Supplier found",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SupplierDetailedResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "Supplier not found",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun getSupplierDetails(@PathVariable id: Long): SupplierDetailedResponse {
        return supplierService.getSupplierDetails(id)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier details")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Supplier updated successfully",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SupplierDetailedResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Supplier not found",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Duplicate supplier name",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateSupplier(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateSupplierRequest
    ): SupplierDetailedResponse {
        return supplierService.updateSupplier(id, request)
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update supplier status (activate/deactivate)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Supplier status updated successfully"),
            ApiResponse(
                responseCode = "400", description = "Invalid request body",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Supplier not found",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "405", description = "Operation Not Allowed (e.g., deactivating default)",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateSupplierStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateSupplierStatusRequest
    ) {
        supplierService.updateSupplierStatus(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supplier")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Supplier deleted successfully"),
            ApiResponse(
                responseCode = "404", description = "Supplier not found",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "405", description = "Operation Not Allowed (e.g., deleting default)",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun deleteSupplier(@PathVariable id: Long) {
        supplierService.deleteSupplier(id)
    }
}