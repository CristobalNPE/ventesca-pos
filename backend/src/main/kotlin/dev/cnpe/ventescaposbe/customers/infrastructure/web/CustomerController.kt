package dev.cnpe.ventescaposbe.customers.infrastructure.web

import dev.cnpe.ventescaposbe.customers.application.dto.request.CreateCustomerRequest
import dev.cnpe.ventescaposbe.customers.application.dto.request.UpdateCustomerRequest
import dev.cnpe.ventescaposbe.customers.application.dto.response.CustomerDetailedResponse
import dev.cnpe.ventescaposbe.customers.application.dto.response.CustomerSummaryResponse
import dev.cnpe.ventescaposbe.customers.application.service.CustomerService
import dev.cnpe.ventescaposbe.security.annotation.RequirePosOperatorRoles
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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/customers")
@Tag(name = "Customers", description = "Manage customer information.")
@RequirePosOperatorRoles
class CustomerController(
    private val customerService: CustomerService
) {

    @PostMapping
    @Operation(summary = "Create a new customer")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Customer created successfully",
                content = [Content(schema = Schema(implementation = CustomerSummaryResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Duplicate email or Tax ID",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun createCustomer(@Valid @RequestBody request: CreateCustomerRequest): ResponseEntity<CustomerSummaryResponse> {
        val createdCustomer = customerService.createCustomer(request)
        return ResponseEntity.created(URI.create("/customers/${createdCustomer.id}")).body(createdCustomer)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer details by ID")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Customer found",
                content = [Content(schema = Schema(implementation = CustomerDetailedResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Customer not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun getCustomerDetails(@PathVariable id: Long): CustomerDetailedResponse {
        return customerService.getCustomerDetails(id)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer details")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Customer created successfully",
                content = [Content(schema = Schema(implementation = CustomerSummaryResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Duplicate email or Tax ID",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun updateCustomer(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCustomerRequest
    ): CustomerDetailedResponse {
        return customerService.updateCustomer(id, request)
    }

    @GetMapping
    @Operation(summary = "List or search customers (paginated)")
    @PageableAsQueryParam
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Customer list retrieved",
                content = [Content(schema = Schema(implementation = PageResponse::class))] //todo fixme
            )
        ]
    )
    fun listCustomers(
        @Parameter(description = "Optional search term for name, email, phone, or tax ID")
        @RequestParam(required = false) searchTerm: String?,
        @Parameter(hidden = true) @PageableDefault(
            size = 20,
            sort = ["personalInfo.firstName", "personalInfo.lastName"]
        ) pageable: Pageable
    ): PageResponse<CustomerSummaryResponse> {
        return customerService.listCustomers(pageable, searchTerm)
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a customer record")
    @ApiResponses(
        ApiResponse(
            responseCode = "201", description = "Customer created successfully",
            content = [Content(schema = Schema(implementation = CustomerSummaryResponse::class))]
        ),
        ApiResponse(
            responseCode = "400", description = "Invalid input data",
            content = [Content(schema = Schema(implementation = ApiResult::class))]
        ),
        ApiResponse(responseCode = "401", description = "Unauthorized"),
        ApiResponse(responseCode = "403", description = "Forbidden")
    )
    fun activateCustomer(@PathVariable id: Long): CustomerDetailedResponse {
        return customerService.activateCustomer(id)
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a customer record")
    @ApiResponses(
        ApiResponse(
            responseCode = "201", description = "Customer created successfully",
            content = [Content(schema = Schema(implementation = CustomerSummaryResponse::class))]
        ),
        ApiResponse(
            responseCode = "400", description = "Invalid input data",
            content = [Content(schema = Schema(implementation = ApiResult::class))]
        ),
        ApiResponse(responseCode = "401", description = "Unauthorized"),
        ApiResponse(responseCode = "403", description = "Forbidden")
    )
    fun deactivateCustomer(@PathVariable id: Long): CustomerDetailedResponse {
        return customerService.deactivateCustomer(id)
    }
}