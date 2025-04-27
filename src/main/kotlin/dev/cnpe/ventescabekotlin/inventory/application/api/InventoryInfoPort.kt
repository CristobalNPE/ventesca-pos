package dev.cnpe.ventescabekotlin.inventory.application.api

import dev.cnpe.ventescabekotlin.inventory.application.api.dto.InventoryItemInfo


interface InventoryInfoPort {

    /**
     * Retrieves basic inventory/stock info for a given product ID.
     *
     * @param productId The ID of the product.
     * @return InventoryItemInfo containing stock details, its assumed that every products has inventory initialized.
     * @throws DomainException if the product is not found or has no inventory items.
     */
    fun getInventoryItemInfo(productId: Long): InventoryItemInfo
}
