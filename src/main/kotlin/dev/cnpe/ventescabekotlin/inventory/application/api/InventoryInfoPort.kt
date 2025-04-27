package dev.cnpe.ventescabekotlin.inventory.application.api

import dev.cnpe.ventescabekotlin.inventory.application.api.dto.BranchInventoryDetails
import dev.cnpe.ventescabekotlin.inventory.application.api.dto.ProductInventorySummary

/**
 * Port defining operations for retrieving essential inventory information needed by other modules.
 */
interface InventoryInfoPort {

    /**
     * Retrieves an aggregated inventory summary for a product across all branches.
     * Provides total stock and representative unit/min quantity.
     *
     * @param productId The ID of the product.
     * @return ProductInventorySummary containing aggregated stock details.
     * @throws DomainException if the product is not found or has no inventory items.
     */
    fun getInventorySummary(productId: Long): ProductInventorySummary

    /**
     * Retrieves detailed inventory information for a specific product within a specific branch.
     *
     * @param productId The ID of the product.
     * @param branchId The ID of the branch.
     * @return BranchInventoryDetails containing specific stock details for that item.
     * @throws DomainException if the inventory item for the product/branch combination is not found.
     */
    fun getBranchInventoryDetails(productId: Long, branchId: Long): BranchInventoryDetails
}
