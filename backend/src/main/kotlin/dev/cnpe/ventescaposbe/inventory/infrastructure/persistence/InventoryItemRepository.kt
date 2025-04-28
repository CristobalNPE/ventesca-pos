package dev.cnpe.ventescaposbe.inventory.infrastructure.persistence

import dev.cnpe.ventescaposbe.inventory.domain.entity.InventoryItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface InventoryItemRepository : JpaRepository<InventoryItem, Long> {


    fun findByProductIdAndBranchId(productId: Long, branchId: Long): InventoryItem?

    fun findAllByProductId(productId: Long): List<InventoryItem>

    fun findAllByBranchId(branchId: Long): List<InventoryItem>

    @Query(
        """
        select coalesce(sum(i.stock.quantity), 0.0)
        from InventoryItem i
        where i.productId = :productId
        """
    )
    fun calculateTotalStockForProduct(productId: Long): Double


}