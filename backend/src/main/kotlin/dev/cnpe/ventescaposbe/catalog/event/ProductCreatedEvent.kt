package dev.cnpe.ventescaposbe.catalog.event

import dev.cnpe.ventescaposbe.inventory.domain.enums.StockUnitType

data class ProductCreatedEvent(
    val productId: Long,
    val unitType: StockUnitType
)
