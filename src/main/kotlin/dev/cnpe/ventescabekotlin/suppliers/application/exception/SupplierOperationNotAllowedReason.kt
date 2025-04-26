package dev.cnpe.ventescabekotlin.suppliers.application.exception

import dev.cnpe.ventescabekotlin.shared.application.exception.OperationNotAllowedReason
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reasons why a supplier operation is not allowed")
enum class SupplierOperationNotAllowedReason : OperationNotAllowedReason {
    IS_DEFAULT_SUPPLIER,
    CANNOT_DEACTIVATE_DEFAULT_SUPPLIER
}