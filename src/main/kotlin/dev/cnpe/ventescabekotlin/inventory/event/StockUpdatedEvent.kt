package dev.cnpe.ventescabekotlin.inventory.event

data class StockUpdatedEvent(
    val productId: Long,
    val totalStockQuantity: Double
)
