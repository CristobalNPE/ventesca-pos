package dev.cnpe.ventescabekotlin.inventory.application.service

import dev.cnpe.ventescabekotlin.business.application.api.BusinessDataPort
import dev.cnpe.ventescabekotlin.catalog.event.ProductCreatedEvent
import dev.cnpe.ventescabekotlin.inventory.application.dto.request.UpdateStockRequest
import dev.cnpe.ventescabekotlin.inventory.application.dto.response.InventoryItemDetailsResponse
import dev.cnpe.ventescabekotlin.inventory.domain.entity.InventoryItem
import dev.cnpe.ventescabekotlin.inventory.domain.entity.StockModification
import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockModificationType
import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockUnitType
import dev.cnpe.ventescabekotlin.inventory.domain.vo.Stock
import dev.cnpe.ventescabekotlin.inventory.event.StockUpdatedEvent
import dev.cnpe.ventescabekotlin.inventory.infrastructure.persistence.InventoryItemRepository
import dev.cnpe.ventescabekotlin.shared.application.exception.DomainException
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
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
    private val messageSource: MessageSource
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
                    type = if (difference > 0) StockModificationType.INCREASE else StockModificationType.DECREASE,
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