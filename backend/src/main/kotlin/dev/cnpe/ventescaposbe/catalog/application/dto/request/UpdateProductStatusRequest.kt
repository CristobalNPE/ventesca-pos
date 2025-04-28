package dev.cnpe.ventescaposbe.catalog.application.dto.request

import dev.cnpe.ventescaposbe.catalog.domain.enums.ProductStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "Request to update the status of a product.")
data class UpdateProductStatusRequest(

    @field:NotNull
    @Schema(description = "The new status for the product.", example = "INACTIVE")
    val status: ProductStatus
)