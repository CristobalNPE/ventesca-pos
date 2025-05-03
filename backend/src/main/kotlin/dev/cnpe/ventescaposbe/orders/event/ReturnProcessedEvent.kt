package dev.cnpe.ventescaposbe.orders.event

/**
 * Event published when a return transaction is successfully processed.
 * Used primarily by the Inventory module to adjust stock levels.
 */
data class ReturnProcessedEvent(
    val returnTransactionId: Long,
    val branchId: Long,
    val itemsToRestock: List<ItemAdjustmentInfo>,
    val itemsToDiscard: List<ItemAdjustmentInfo>
)