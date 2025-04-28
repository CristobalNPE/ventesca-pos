package dev.cnpe.ventescaposbe.brands.infrastructure.web

import dev.cnpe.ventescaposbe.brands.application.dto.request.CreateBrandRequest
import dev.cnpe.ventescaposbe.brands.application.dto.response.BrandSummaryResponse
import dev.cnpe.ventescaposbe.brands.application.service.BrandService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/brands")
@Tag(name = "Brands", description = "Manage brands in the business.")
class BrandController(
    private val brandService: BrandService
) {

    @PostMapping
    @Operation(summary = "Register a new brand", description = "Creates a new brand in the business.")
    @ApiResponse(
        responseCode = "201",
        description = "Brand created successfully",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = BrandSummaryResponse::class)
        )]
    )
    @ApiResponse(
        responseCode = "409",
        description = "Invalid input (e.g., duplicate name)"
    )
    fun registerBrand(@RequestBody @Valid brandRequest: CreateBrandRequest): ResponseEntity<BrandSummaryResponse> {
        val createdBrand = brandService.registerBrand(brandRequest)
        return ResponseEntity
            .created(URI.create("/brands/${createdBrand.id}"))
            .body(createdBrand)
    }

}