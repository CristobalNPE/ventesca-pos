package dev.cnpe.ventescaposbe.orders.event

/**
 * Data carrier for information about a single item sold within an order,
 * used within the OrderCompletedEvent.
 */
data class ItemSoldInfo(
    val productId: Long,
    val quantity: Double
)
