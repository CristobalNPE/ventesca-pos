package dev.cnpe.ventescaposbe.suppliers.application.exception

import dev.cnpe.ventescaposbe.shared.application.exception.OperationNotAllowedReason
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reasons why a supplier operation is not allowed")
enum class SupplierOperationNotAllowedReason : OperationNotAllowedReason {
    @Schema(description = "Cannot operate on the default supplier.")
    IS_DEFAULT_SUPPLIER,

    @Schema(description = "Cannot deactivate the default supplier.")
    CANNOT_DEACTIVATE_DEFAULT_SUPPLIER,

    @Schema(description = "Cannot delete supplier because it has associated products.")
    SUPPLIER_HAS_PRODUCTS
}