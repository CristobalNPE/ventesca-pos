package dev.cnpe.ventescaposbe.catalog.infrastructure.web

import dev.cnpe.ventescaposbe.catalog.application.dto.request.*
import dev.cnpe.ventescaposbe.catalog.application.dto.response.ProductCreatedResponse
import dev.cnpe.ventescaposbe.catalog.application.dto.response.ProductDetailedResponse
import dev.cnpe.ventescaposbe.catalog.application.dto.response.ProductPriceInfoResponse
import dev.cnpe.ventescaposbe.catalog.application.dto.response.ProductSummaryResponse
import dev.cnpe.ventescaposbe.catalog.application.service.ProductService
import dev.cnpe.ventescaposbe.catalog.application.service.ProductUpdateService
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Manage products in the catalog.")
class ProductController(
    private val productService: ProductService,
    private val productUpdateService: ProductUpdateService
) {

    @PostMapping("/draft")
    @Operation(summary = "Create a product draft with minimal info")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Product draft created successfully",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProductCreatedResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data", content = [Content(
                    schema = Schema(
                        implementation = ApiResult::class
                    )
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (Requires BUSINESS_ADMIN role)"),
            ApiResponse(
                responseCode = "409",
                description = "Conflict (e.g., duplicate name or barcode)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun createProductDraft(@Valid @RequestBody request: CreateProductDraftRequest): ResponseEntity<ProductCreatedResponse> {
        val created = productService.createProductDraft(request)
        return ResponseEntity.created(URI.create("/products/${created.id}")).body(created)
    }

    @PostMapping
    @Operation(summary = "Create a full product with all details")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Product created successfully",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProductCreatedResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data (validation or relation errors)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (Requires BUSINESS_ADMIN role)"),
            ApiResponse(
                responseCode = "409",
                description = "Conflict (e.g., duplicate name or barcode)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ResponseEntity<ProductCreatedResponse> {
        val created = productService.createProduct(request)
        return ResponseEntity.created(URI.create("/products/${created.id}")).body(created)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details by ID")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Product details retrieved",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProductDetailedResponse::class)
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun getProductDetails(@PathVariable id: Long): ProductDetailedResponse {
        return productService.getProductDetails(id)
    }

    @GetMapping
    @Operation(summary = "Get product summaries (paginated)")
    @PageableAsQueryParam
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Product list retrieved",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PageResponse::class) //TODO: Check this, might not include the correct typing PageResponse<ProductSummaryResponse> is this smart enough?
                )]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun getAllProductSummaries(@Parameter(hidden = true) pageable: Pageable): PageResponse<ProductSummaryResponse> {
        return productService.getAllProductSummaries(pageable)
    }

    @GetMapping("/{id}/prices")
    @Operation(summary = "Get product price history (paginated)")
    @PageableAsQueryParam
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Price history retrieved",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = PageResponse::class)
                )] //TODO Check here too
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun getProductPriceHistory(
        @PathVariable id: Long,
        @Parameter(hidden = true) pageable: Pageable
    ): PageResponse<ProductPriceInfoResponse> {
        return productService.getProductPriceHistory(id, pageable)
    }

    // *******************************
    // 🔰 Update Endpoints
    // *******************************

    @PutMapping("/{id}/basics")
    @Operation(summary = "Update product basic details (name, description)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Product basics updated successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (Requires BUSINESS_ADMIN role)"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict (duplicate name)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateBasics(@PathVariable id: Long, @Valid @RequestBody request: UpdateProductBasicsRequest) {
        productUpdateService.updateBasics(id, request)
    }

    @PutMapping("/{id}/price")
    @Operation(summary = "Update product selling price")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Selling price updated successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (Requires BUSINESS_ADMIN role)"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateSellingPrice(@PathVariable id: Long, @Valid @RequestBody request: UpdateProductSellingPriceRequest) {
        productUpdateService.updateSellingPrice(id, request)
    }

    @PutMapping("/{id}/cost")
    @Operation(summary = "Update product supplier cost")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Supplier cost updated successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (Requires BUSINESS_ADMIN role)"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateSupplierCost(@PathVariable id: Long, @Valid @RequestBody request: UpdateProductSupplierCostRequest) {
        productUpdateService.updateSupplierCost(id, request)
    }

    @PutMapping("/{id}/relations")
    @Operation(summary = "Update product relations (category, brand, supplier)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Product relations updated successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data (validation or relation ID not found)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (Requires BUSINESS_ADMIN role)"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateRelations(@PathVariable id: Long, @Valid @RequestBody request: UpdateProductRelationsRequest) {
        productUpdateService.updateRelations(id, request)
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update product status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Product status updated successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input data",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (Requires BUSINESS_ADMIN role)"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun updateStatus(@PathVariable id: Long, @Valid @RequestBody request: UpdateProductStatusRequest) {
        productUpdateService.updateStatus(id, request)
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a product (sets status to ACTIVE after validation)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Product activated successfully"),
            ApiResponse(
                responseCode = "400",
                description = "Invalid State (product cannot be activated, e.g., missing price/relations)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden (Requires BUSINESS_ADMIN role)"),
            ApiResponse(
                responseCode = "404",
                description = "Product not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            )
        ]
    )
    fun activateProduct(@PathVariable id: Long) {
        productUpdateService.activateProduct(id)
    }

    // --- Delete Endpoint ---
    // TODO: Implement DELETE /products/{id} if needed - Requires careful consideration of dependencies (sales, inventory)
    // @DeleteMapping("/{id}")
    // // Security: Covered by 'DELETE /products/**' rule in SecurityConfig (requires BUSINESS_ADMIN)
    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // @Operation(summary = "Delete a product")
    // @ApiResponses(value = [/* Add responses */])
    // fun deleteProduct(@PathVariable id: Long) { /* ... service call ... */ }

}