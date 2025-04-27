package dev.cnpe.ventescabekotlin.catalog.event

import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockUnitType

data class ProductCreatedEvent(
    val productId: Long,
    val unitType: StockUnitType
)
