package dev.cnpe.ventescaposbe.categories.infrastructure.web

import dev.cnpe.ventescaposbe.categories.application.dto.request.CreateCategoryRequest
import dev.cnpe.ventescaposbe.categories.application.dto.request.UpdateCategoryRequest
import dev.cnpe.ventescaposbe.categories.application.dto.response.CategoryCreatedResponse
import dev.cnpe.ventescaposbe.categories.application.dto.response.CategoryDetailedResponse
import dev.cnpe.ventescaposbe.categories.application.dto.response.CategoryWithChildrenResponse
import dev.cnpe.ventescaposbe.categories.application.service.CategoryService
import dev.cnpe.ventescaposbe.security.annotation.RequireAdmin
import io.swagger.v3.oas.annotations.Operation
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
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Manage categories in the business.")
class CategoryController(
    private val categoryService: CategoryService
) {

    @PostMapping
    @RequireAdmin
    @Operation(summary = "Create a new category", description = "Creates a new category in the business.")
    @ApiResponse(
        responseCode = "201",
        description = "Category created successfully",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = CategoryCreatedResponse::class)
        )]
    )
    @ApiResponse(
        responseCode = "409",
        description = "Invalid input (e.g., duplicate name)"
    )
    fun createCategory(@RequestBody @Valid request: CreateCategoryRequest): ResponseEntity<CategoryCreatedResponse> {
        val created = categoryService.createCategory(request)
        return ResponseEntity.created(URI.create("/brands/${created.id}")).body(created)
    }

    @PostMapping("/{parentId}/subcategories")
    @RequireAdmin
    @Operation(summary = "Create a subcategory", description = "Adds a subcategory to an existing category.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Subcategory created successfully",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CategoryCreatedResponse::class)
                )]
            ),
            ApiResponse(responseCode = "409", description = "Invalid input (e.g., duplicate name)"),
            ApiResponse(responseCode = "404", description = "Parent category not found"),
            ApiResponse(responseCode = "405", description = "Cannot add subcategory to default category"),
            ApiResponse(responseCode = "405", description = "Maximum depth reached")
        ]
    )
    fun addSubcategory(
        @PathVariable(name = "parentId") parentId: Long,
        @RequestBody @Valid request: CreateCategoryRequest
    ): ResponseEntity<CategoryCreatedResponse> {

        val createdSubcategory = categoryService.addSubcategory(request, parentId)

        return ResponseEntity
            .created(URI.create("/categories/${createdSubcategory.id}"))
            .body(createdSubcategory)
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves a list of all categories in the business.")
    @ApiResponse(
        responseCode = "200",
        description = "List of categories retrieved successfully",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = CategoryWithChildrenResponse::class, type = "array")
            )
        ]
    )
    fun getAllCategories(): List<CategoryWithChildrenResponse> {
        return categoryService.getAllCategories()
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a single category by its unique identifier.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Category retrieved successfully",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CategoryDetailedResponse::class)
                )]
            ),
            ApiResponse(responseCode = "404", description = "Category not found")
        ]
    )
    fun getCategoryById(@PathVariable(name = "id") id: Long): CategoryDetailedResponse {
        return categoryService.getCategoryDetails(id)
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete category by ID", description = "Deletes a single category by its unique identifier.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            ApiResponse(responseCode = "404", description = "Category not found")
        ]
    )
    fun deleteCategoryById(@PathVariable(name = "id") id: Long) {
        categoryService.deleteCategory(id)
    }

    @PutMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "Update category by ID", description = "Updates a single category by its unique identifier.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Category updated successfully",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CategoryDetailedResponse::class)
                )]
            ),
            ApiResponse(responseCode = "404", description = "Category not found"),
            ApiResponse(responseCode = "409", description = "Invalid input (e.g., duplicate name)")
        ]
    )
    fun updateCategory(
        @PathVariable(name = "id") id: Long,
        @RequestBody @Valid request: UpdateCategoryRequest
    ): CategoryDetailedResponse {
        return categoryService.updateCategory(id, request)
    }

}