package dev.cnpe.ventescabekotlin.brands.application.exception

import dev.cnpe.ventescabekotlin.shared.application.exception.OperationNotAllowedReason

enum class BrandOperationNotAllowedReason: OperationNotAllowedReason {
    IS_DEFAULT_BRAND
}