package dev.cnpe.ventescaposbe.orders.event

/**
 * Details of a single item adjustment within the ReturnProcessedEvent.
 */
data class ItemAdjustmentInfo(
    val productId: Long,
    val quantity: Double
)