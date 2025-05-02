package dev.cnpe.ventescaposbe.orders.event

/**
 * Event published when an order is successfully marked as COMPLETED.
 * Used primarily by the Inventory module to update stock levels.
 */
data class OrderCompletedEvent(
    val orderId: Long,
    val branchId:Long,
    val itemsSold: List<ItemSoldInfo>
)
