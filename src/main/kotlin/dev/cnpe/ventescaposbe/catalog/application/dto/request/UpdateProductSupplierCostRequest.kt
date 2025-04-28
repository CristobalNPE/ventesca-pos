package dev.cnpe.ventescaposbe.catalog.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Schema(description = "Request to update the supplier cost of a product.")
data class UpdateProductSupplierCostRequest(

    @field:NotNull
    @field:DecimalMin(value = "0.01", message = "Supplier cost must be positive")
    @Schema(description = "The new supplier cost amount.", example = "780.00")
    val supplierCost: BigDecimal
)