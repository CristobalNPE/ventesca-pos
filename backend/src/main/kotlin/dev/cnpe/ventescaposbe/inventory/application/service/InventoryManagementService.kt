package dev.cnpe.ventescaposbe.inventory.application.service

import dev.cnpe.ventescaposbe.business.application.api.BusinessDataPort
import dev.cnpe.ventescaposbe.catalog.event.ProductCreatedEvent
import dev.cnpe.ventescaposbe.inventory.application.dto.request.AdjustStockRequest
import dev.cnpe.ventescaposbe.inventory.application.dto.request.UpdateStockRequest
import dev.cnpe.ventescaposbe.inventory.domain.entity.InventoryItem
import dev.cnpe.ventescaposbe.inventory.domain.entity.StockModification
import dev.cnpe.ventescaposbe.inventory.domain.enums.StockModificationReason
import dev.cnpe.ventescaposbe.inventory.domain.enums.StockModificationType.DECREASE
import dev.cnpe.ventescaposbe.inventory.domain.enums.StockModificationType.INCREASE
import dev.cnpe.ventescaposbe.inventory.domain.enums.StockUnitType
import dev.cnpe.ventescaposbe.inventory.domain.vo.Stock
import dev.cnpe.ventescaposbe.inventory.event.StockUpdatedEvent
import dev.cnpe.ventescaposbe.inventory.infrastructure.persistence.InventoryItemRepository
import dev.cnpe.ventescaposbe.orders.event.OrderCompletedEvent
import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.math.abs
import kotlin.math.round

private val log = KotlinLogging.logger {}

@Service
@Transactional
class InventoryManagementService(
    private val inventoryItemRepository: InventoryItemRepository,
    private val businessDataPort: BusinessDataPort,
    private val eventPublisher: ApplicationEventPublisher,
) {

    /**
     * Updates the stock for a specific product in a specific branch.
     *
     * @param productId The ID of the product to update.
     * @param request The details of the stock update.
     * @throws DomainException if data is invalid (e.g., reason missing, item not found).
     */
    fun updateStock(productId: Long, request: UpdateStockRequest) {
        log.debug { "Attempting stock update for Product ID: $productId in Branch ID: ${request.branchId}" }

        val item = findInventoryItemOrThrow(productId, request.branchId)
        val originalStock = item.stock

        val currentQuantity = originalStock.quantity
        var newQuantity = request.stockQuantity
        val quantityChanged = currentQuantity != newQuantity

        if (quantityChanged && request.reason == null) {
            throw DomainException(GeneralErrorCode.INVALID_DATA, details = mapOf("reason" to "REASON_NOT_PROVIDED"))
        }

        if (quantityChanged) {
            val reason = request.reason!!
            var difference = newQuantity - currentQuantity


            if (request.unitOfMeasure == StockUnitType.UNIT) {
                newQuantity = round(newQuantity)
                val roundedCurrentQuantity = round(currentQuantity)

                if (roundedCurrentQuantity != currentQuantity) {
                    difference = newQuantity - roundedCurrentQuantity
                }
            }

            if (difference != 0.0) {
                val modification = StockModification(
                    amount = abs(difference),
                    type = if (difference > 0) INCREASE else DECREASE,
                    reason = reason,
                    item = item,
                )
                item.addStockModification(modification)
                log.info {
                    "Added StockModification: Type=${modification.type}, Amount=${modification.amount}, " +
                            "Reason=${modification.reason} for Product ID $productId / Branch ${request.branchId}"
                }
            } else {
                log.info {
                    "Stock quantity difference is zero after rounding for Product ID $productId / Branch ${request.branchId}." +
                            " No modification recorded."
                }
            }
        }
        val newStockVO = Stock(
            quantity = newQuantity,
            minimumQuantity = request.minimumStockLevel,
            unit = request.unitOfMeasure
        )

        if (originalStock.unit != newStockVO.unit) {
            log.warn {
                "Stock unit change detected for Product ID $productId from" +
                        " ${originalStock.unit} to ${newStockVO.unit}. Updating all related items."
            }
            updateStockUnitForAllItemsOfProduct(productId, newStockVO.unit)
            item.stock = newStockVO
        } else {
            item.stock = newStockVO
        }

        inventoryItemRepository.save(item)
        log.info { "Saved updated InventoryItem for Product ID $productId / Branch ${request.branchId}. New Stock: ${item.stock}" }

        val totalStockQuantity = inventoryItemRepository.calculateTotalStockForProduct(productId)
        eventPublisher.publishEvent(StockUpdatedEvent(productId, totalStockQuantity))
        log.info { "Published StockUpdatedEvent for Product ID $productId. New Total Stock: $totalStockQuantity" }
    }


    /**
     * Manually adjusts the stock quantity for a specific product in a specific branch
     * for reasons like damage, loss, correction, or manual restock.
     *
     * @param productId The ID of the product whose stock is being adjusted.
     * @param request The details of the stock adjustment, including branch, amount, and reason.
     * @throws DomainException if the inventory item is not found, or if the reason is invalid for manual adjustment.
     */
    fun adjustStock(productId: Long, request: AdjustStockRequest) {
        log.info {
            "Adjusting stock for Product ID: $productId in Branch ID: ${request.branchId}. " +
                    "Amount: ${request.adjustmentAmount}, Reason: ${request.reason}, Notes: ${request.notes ?: "N/A"}"
        }

        val item = findInventoryItemOrThrow(productId, request.branchId)
        val originalStockVO = item.stock

        val modificationType = if (request.adjustmentAmount > 0) INCREASE else DECREASE

        val absoluteAdjustmentAmount = abs(request.adjustmentAmount)

        val preliminaryNewQuantity = originalStockVO.quantity + request.adjustmentAmount
        val newQuantity = preliminaryNewQuantity.coerceAtLeast(0.0)

        if (newQuantity != preliminaryNewQuantity) {
            log.warn {
                "Stock adjustment for Product ID $productId / Branch ${request.branchId} resulted in negative quantity ($preliminaryNewQuantity). " +
                        "Capping stock at 0. Original: ${originalStockVO.quantity}, Adjustment: ${request.adjustmentAmount}"
            }
        }

        val modification = StockModification(
            amount = absoluteAdjustmentAmount,
            type = modificationType,
            reason = request.reason,
            item = item,
        )
        //TODO: SHould we add a 'notes' field to StockModification entity:?

        item.addStockModification(modification)
        log.debug { "Created StockModification record for adjustment." }

        item.stock = originalStockVO.copy(quantity = newQuantity)
        log.debug { "Updated InventoryItem stock VO. New quantity: $newQuantity" }

        inventoryItemRepository.save(item)
        log.info { "Saved InventoryItem (ID: ${item.id}) after stock adjustment." }

        val totalStockQuantity = inventoryItemRepository.calculateTotalStockForProduct(productId)

        eventPublisher.publishEvent(StockUpdatedEvent(productId, totalStockQuantity))
        log.info { "Published StockUpdatedEvent for Product ID $productId. New Total Stock: $totalStockQuantity" }
    }

    @ApplicationModuleListener
    fun onProductCreated(event: ProductCreatedEvent) {
        log.info { "Received ProductCreatedEvent for Product ID: ${event.productId}, Unit: ${event.unitType}. Creating initial inventory items..." }

        val branchIds = try {
            businessDataPort.getBusinessBranchIds()
        } catch (e: Exception) {
            log.error(e) { "Failed to retrieve branch IDs while handling ProductCreatedEvent for Product ID ${event.productId}. Cannot create inventory items." }
            return
        }

        if (branchIds.isEmpty()) {
            log.warn { "No branches found for the business when creating inventory items for Product ID ${event.productId}. This might indicate an issue or a new business with no branches yet." }
            return
        }
        log.debug { "Found ${branchIds.size} branches. Creating InventoryItem for Product ID ${event.productId} in each." }

        val inventoryItems = branchIds.map { branchId ->
            InventoryItem.forNewProduct(
                productId = event.productId,
                branchId = branchId,
                unitType = event.unitType
            )
        }

        try {
            val savedItems = inventoryItemRepository.saveAll(inventoryItems)
            log.info { "🌿 Created ${savedItems.size} Inventory Items for Product ID ${event.productId} across ${branchIds.size} branch(es)." }
        } catch (e: Exception) {
            log.error(e) { "Failed to save initial inventory items for Product ID ${event.productId}." }
        }
    }


    /**
     * Listens for OrderCompletedEvent and updates inventory stock levels accordingly.
     * Decreases stock for each item sold in the specified branch.
     * Publishes a StockUpdatedEvent for each product affected.
     *
     * @param event The OrderCompletedEvent containing details of the completed order.
     */
    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onOrderCompleted(event: OrderCompletedEvent) {
        log.info {
            "Received OrderCompletedEvent for Order ID: ${event.orderId}, " +
                    "Branch ID: ${event.branchId}. Processing ${event.itemsSold.size} item(s) for stock update."
        }

        var stockUpdated = false

        event.itemsSold.forEach { itemSold ->
            try {
                val inventoryItem =
                    inventoryItemRepository.findByProductIdAndBranchId(itemSold.productId, event.branchId)

                if (inventoryItem == null) {
                    // Should never happen, if inventory items are created correctly. Defensive check.
                    log.error {
                        "Inventory item not found for Product ID ${itemSold.productId} in Branch ${event.branchId} " +
                                "while processing OrderCompletedEvent ${event.orderId}. Cannot update stock for this item."
                    }
                    // TODO: Consider raising an alert or adding to a reconciliation queue.
                    return@forEach
                }

                val quantitySold = itemSold.quantity
                val originalQuantity = inventoryItem.currentQuantity

                log.debug {
                    "Updating stock for Product ID ${itemSold.productId} (Branch ${event.branchId}). " +
                            "Original Qty: $originalQuantity, Quantity Sold: $quantitySold."
                }

                val modification = StockModification(
                    amount = quantitySold,
                    type = DECREASE,
                    reason = StockModificationReason.SALE,
                    item = inventoryItem,
                )
                inventoryItem.addStockModification(modification)

                inventoryItem.stock = inventoryItem.stock.removeQuantity(quantitySold)

                inventoryItemRepository.save(inventoryItem)
                stockUpdated = true

                log.info {
                    "Stock updated for Product ID ${itemSold.productId} (Branch ${event.branchId}). " +
                            "New Quantity: ${inventoryItem.stock.quantity}. Recorded SALE modification."
                }

                val totalStockQuantity =
                    inventoryItemRepository.calculateTotalStockForProduct(itemSold.productId)
                eventPublisher.publishEvent(StockUpdatedEvent(itemSold.productId, totalStockQuantity))
                log.debug { "Published StockUpdatedEvent for Product ID ${itemSold.productId}. New Total Stock: $totalStockQuantity" }

            } catch (e: Exception) {
                log.error(e) {
                    "Failed to process stock update for Product ID ${itemSold.productId} (Branch ${event.branchId}) " +
                            "from OrderCompletedEvent ${event.orderId}. Order remains completed."
                }
                // TODO: Implement retry logic or dead-letter queue for failed event processing.
            }
        }

        if (stockUpdated) {
            log.info { "Finished processing stock updates for OrderCompletedEvent ${event.orderId}." }
        } else {
            log.warn { "No stock updates seemed necessary or possible for OrderCompletedEvent ${event.orderId}." }
        }
    }


    // FIXME: Implement branch deletion logic listener if needed
    // @ApplicationModuleListener
    // fun onBranchDeleted(event: BranchDeletedEvent) { ... }


    // *******************************
    // 🔰 Private Helpers
    // *******************************


    private fun updateStockUnitForAllItemsOfProduct(productId: Long, newUnit: StockUnitType) {
        val itemsToUpdate = inventoryItemRepository.findAllByProductId(productId)
        var count = 0
        itemsToUpdate.forEach { item ->
            if (item.stock.unit != newUnit) {
                item.stock = item.stock.changeUnit(newUnit)
//                inventoryItemRepository.saveAll(itemsToUpdate)
                count++
            }
        }
        if (count > 0) {
            inventoryItemRepository.saveAll(itemsToUpdate)
            log.info { "Updated stock unit to [$newUnit] for $count inventory items of Product ID [$productId]" }
        } else {
            log.debug { "No stock units needed updating for Product ID [$productId] to [$newUnit]" }
        }
    }


    private fun findInventoryItemOrThrow(productId: Long, branchId: Long): InventoryItem {
        return inventoryItemRepository.findByProductIdAndBranchId(productId, branchId)
            ?: throw DomainException(
                errorCode = GeneralErrorCode.RESOURCE_NOT_FOUND,
                details = mapOf("productId" to productId, "branchId" to branchId)
            )
    }


}