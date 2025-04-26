package dev.cnpe.ventescabekotlin.inventory.domain.enums

import dev.cnpe.ventescabekotlin.shared.domain.enums.DomainEnum


import io.swagger.v3.oas.annotations.media.Schema

/**
 * Represents the available unit types for measuring and tracking stock quantities in the inventory system.
 */
@Schema(
    description = "Available unit types for measuring and tracking stock quantities",
//    enumAsRef = false
)
enum class StockUnitType : DomainEnum {

    @Schema(description = "Individual units or pieces")
    UNIT,

    @Schema(description = "Weight measurement in kilograms")
    KILOGRAM,

    @Schema(description = "Volume measurement in liters")
    LITER
    
}