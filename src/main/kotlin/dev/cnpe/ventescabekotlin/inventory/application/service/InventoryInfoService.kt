package dev.cnpe.ventescabekotlin.inventory.application.service

import dev.cnpe.ventescabekotlin.inventory.application.api.InventoryInfoPort
import dev.cnpe.ventescabekotlin.inventory.application.api.dto.BranchInventoryDetails
import dev.cnpe.ventescabekotlin.inventory.application.api.dto.ProductInventorySummary
import dev.cnpe.ventescabekotlin.inventory.infrastructure.persistence.InventoryItemRepository
import dev.cnpe.ventescabekotlin.shared.application.exception.DomainException
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode
import dev.cnpe.ventescabekotlin.shared.application.exception.createResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class InventoryInfoService(
    private val inventoryItemRepository: InventoryItemRepository
) : InventoryInfoPort {

    /**
     * Retrieves aggregated inventory information for a product across all branches.
     * Sums the quantities. Assumes minimum quantity and unit are consistent across branches,
     * taking the values from the first retrieved item.
     */
    override fun getInventorySummary(productId: Long): ProductInventorySummary {
        log.debug { "Fetching aggregated inventory info for Product ID: $productId" }
        val items = inventoryItemRepository.findAllByProductId(productId)

        if (items.isEmpty()) {
            log.warn { "No inventory items found for Product ID: $productId during summary aggregation." }
            throw createResourceNotFoundException(
                entityType = "InventoryItem",
                id = productId
            )
        }

        val totalStock = inventoryItemRepository.calculateTotalStockForProduct(productId)

        val firstItemStock = items.first().stock
        val representativeMinQuantity = firstItemStock.minimumQuantity
        val representativeUnit = firstItemStock.unit

        val distinctUnits = items.map { it.stock.unit }.distinct()
        if (distinctUnits.size > 1) {
            log.warn {
                "Inconsistent stock units found for Product ID: $productId across branches: $distinctUnits. " +
                        "Using unit from first item found: $representativeUnit."
            }
        }

        val distinctMinQuantities = items.map { it.stock.minimumQuantity }.distinct()
        if (distinctMinQuantities.size > 1) {
            log.warn {
                "Inconsistent minimum quantities found for Product ID: $productId across branches: $distinctMinQuantities. " +
                        "Using minimum quantity from first item found: $representativeMinQuantity."
            }
        }

        log.debug { "Aggregated inventory info for Product ID: $productId - TotalStock: $totalStock, Representative MinQty: $representativeMinQuantity, Representative Unit: $representativeUnit" }
        return ProductInventorySummary(
            totalStockQuantity = totalStock,
            representativeMinimumQuantity = representativeMinQuantity,
            unitOfMeasure = representativeUnit
        )
    }


    /**
     * Retrieves detailed inventory information for a specific product within a specific branch.
     */
    override fun getBranchInventoryDetails(
        productId: Long,
        branchId: Long
    ): BranchInventoryDetails {
        log.debug { "Fetching branch inventory details for Product ID: $productId, Branch ID: $branchId" }
        val item = inventoryItemRepository.findByProductIdAndBranchId(productId, branchId)
            ?: throw DomainException(
                errorCode = GeneralErrorCode.RESOURCE_NOT_FOUND,
                details = mapOf("productId" to productId, "branchId" to branchId)
            )
        val stock = item.stock

        return BranchInventoryDetails(
            productId = item.productId,
            branchId = item.branchId,
            currentQuantity = stock.quantity,
            minQuantity = stock.minimumQuantity,
            unitOfMeasure = stock.unit,
            isLowStock = stock.isLowStock,
            isOutOfStock = stock.isOutOfStock,
            availableQuantity = stock.availableQuantity,
            needsRestock = stock.needsRestock
        )
    }
}