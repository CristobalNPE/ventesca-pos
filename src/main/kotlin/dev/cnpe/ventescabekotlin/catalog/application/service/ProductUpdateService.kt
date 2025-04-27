package dev.cnpe.ventescabekotlin.catalog.application.service

import dev.cnpe.ventescabekotlin.business.application.api.BusinessDataPort
import dev.cnpe.ventescabekotlin.catalog.application.dto.request.*
import dev.cnpe.ventescabekotlin.catalog.application.util.ProductUtils
import dev.cnpe.ventescabekotlin.catalog.application.validation.ProductRelationValidator
import dev.cnpe.ventescabekotlin.catalog.domain.enums.PriceChangeReason
import dev.cnpe.ventescabekotlin.catalog.domain.model.Product
import dev.cnpe.ventescabekotlin.catalog.domain.model.ProductPrice
import dev.cnpe.ventescabekotlin.catalog.infrastructure.persistence.ProductRepository
import dev.cnpe.ventescabekotlin.currency.service.MoneyFactory
import dev.cnpe.ventescabekotlin.inventory.event.StockUpdatedEvent
import dev.cnpe.ventescabekotlin.shared.application.exception.DomainException
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode
import dev.cnpe.ventescabekotlin.shared.application.exception.createDuplicatedResourceException
import dev.cnpe.ventescabekotlin.shared.application.exception.createResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional
class ProductUpdateService(
    private val productRepository: ProductRepository,
    private val businessDataPort: BusinessDataPort,
    private val moneyFactory: MoneyFactory,
    private val productUtils: ProductUtils,
    private val relationValidator: ProductRelationValidator
) {

    fun updateBasics(id: Long, request: UpdateProductBasicsRequest) {
        val product = findProductByIdOrThrow(id)
        log.debug { "Updating basics for product ID: ${product.id}" }

        var updated = false
        request.name?.takeIf { it.isNotBlank() && it != product.name }?.let { newName ->
            if (productRepository.existsByName(newName)) {
                throw createDuplicatedResourceException("name", newName)
            }
            product.name = newName
            log.trace { "Product $id name updated to '$newName'" }
            updated = true
        }

        // description can be set to blank if needed
        request.description?.takeIf { it != product.description }?.let {
            product.description = it.trim()
            log.trace { "Product $id description updated" }
            updated = true
        }
        if (updated) log.info { "Product basics updated for ID: $id" }
        else log.info { "No changes detected in product basics for ID: $id" }
    }

    fun updateSellingPrice(id: Long, request: UpdateProductSellingPriceRequest) {
        val product = findProductByIdOrThrow(id)
        val currentPrice = product.getCurrentPrice()
            ?: throw DomainException(GeneralErrorCode.INVALID_STATE, details = mapOf("reason" to "NO_ACTIVE_PRICE"))

        val businessPaymentData = businessDataPort.getBusinessPaymentData()
        val currencyCode = currentPrice.sellingPrice.currencyCode

        val grossSellingPrice = if (request.taxInclusive) {
            // TODO: Maybe validate that request.sellingPrice > cost if taxInclusive?
            request.sellingPrice
        } else {
            productUtils.calculateFinalPrice(request.sellingPrice, businessPaymentData)
        }

        if (currentPrice.sellingPrice.amount == grossSellingPrice) {
            log.warn { "Requested selling price results in the same gross price (${grossSellingPrice}). No update performed." }
            return
        }

        log.debug { "Updating selling price for product ID: $id to $grossSellingPrice $currencyCode (Reason: ${request.reason})" }

        val newPriceRecord = ProductPrice(
            sellingPrice = moneyFactory.createMoney(grossSellingPrice, currencyCode),
            supplierCost = currentPrice.supplierCost,
            reason = request.reason,
            product = product
        )
        product.addPrice(newPriceRecord)
        log.info { "Selling price updated for product $id. New active price: ${newPriceRecord.sellingPrice}" }
    }

    fun updateSupplierCost(id: Long, request: UpdateProductSupplierCostRequest) {
        val product = findProductByIdOrThrow(id)
        val currentPrice = product.getCurrentPrice()
            ?: throw DomainException(GeneralErrorCode.INVALID_STATE, details = mapOf("reason" to "NO_ACTIVE_PRICE"))

        val currencyCode = currentPrice.supplierCost.currencyCode
        val newCost = moneyFactory.createMoney(request.supplierCost, currencyCode)

        if (currentPrice.supplierCost == newCost) {
            log.warn { "Requested supplier cost ${request.supplierCost} is same as current. No update performed." }
            return
        }

        log.debug { "Updating supplier cost for product ID: $id to $newCost" }

        val newPriceRecord = ProductPrice(
            sellingPrice = currentPrice.sellingPrice,
            supplierCost = newCost,
            reason = PriceChangeReason.COST_CHANGE,
            product = product
        )
        product.addPrice(newPriceRecord)

        log.info { "Supplier cost updated for product $id. New active cost: ${newPriceRecord.supplierCost}" }
    }

    fun updateRelations(id: Long, request: UpdateProductRelationsRequest) {
        val product = findProductByIdOrThrow(id)
        log.debug { "Updating relations for product ID: $id to Cat=${request.categoryId}, Brand=${request.brandId}, Sup=${request.supplierId}" }

        relationValidator.validateRelations(
            categoryId = request.categoryId,
            brandId = request.brandId,
            supplierId = request.supplierId
        )

        var updated = false
        if (product.categoryId != request.categoryId) {
            product.categoryId = request.categoryId
            updated = true
        }
        if (product.brandId != request.brandId) {
            product.brandId = request.brandId
            updated = true
        }
        if (product.supplierId != request.supplierId) {
            product.supplierId = request.supplierId
            updated = true
        }

        if (updated) {
            log.info { "Product relations updated for ID: $id" }
        } else {
            log.info { "No changes detected in product relations for ID: $id" }
        }
    }

    fun updateStatus(id: Long, request: UpdateProductStatusRequest) {
        val product = findProductByIdOrThrow(id)
        if (product.status != request.status) {
            log.info { "Updating product $id status from ${product.status} to ${request.status}" }
            product.status = request.status
        }
    }

    /**
     * Activates a product by setting its status to ACTIVE.
     * Requires validation checks to be passed.
     */
    fun activateProduct(id: Long) {
        val product = findProductByIdOrThrow(id)
        log.info { "Attempting to activate product: ${product.name} (ID: $id)" }
        product.activate()
        log.info { "🟢 Product activated: ${product.name} (ID: $id)" }
    }


    @ApplicationModuleListener
    fun onStockUpdated(event: StockUpdatedEvent) {
        log.debug { "Received stock update for product ${event.productId}, new total: ${event.totalStockQuantity}" }
        productRepository.findByIdOrNull(event.productId)?.let { product ->
            if (product.totalCurrentStock != event.totalStockQuantity) {
                product.totalCurrentStock = event.totalStockQuantity
                log.info { "Updated totalCurrentStock for product ${product.id} to ${product.totalCurrentStock}" }
            }
        } ?: log.warn { "Received stock update for non-existent product ID: ${event.productId}" }
    }


    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun findProductByIdOrThrow(id: Long): Product {
        return productRepository.findByIdOrNull(id)
            ?: throw createResourceNotFoundException("Product", id)
    }

}