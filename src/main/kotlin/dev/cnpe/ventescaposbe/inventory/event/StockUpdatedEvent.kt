package dev.cnpe.ventescaposbe.inventory.event

data class StockUpdatedEvent(
    val productId: Long,
    val totalStockQuantity: Double
)
