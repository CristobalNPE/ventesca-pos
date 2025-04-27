package dev.cnpe.ventescabekotlin.catalog.application.mapper

import dev.cnpe.ventescabekotlin.catalog.application.dto.common.ProductDetails
import dev.cnpe.ventescabekotlin.catalog.application.dto.common.ProductPricingData
import dev.cnpe.ventescabekotlin.catalog.application.dto.common.ProductRelationsData
import dev.cnpe.ventescabekotlin.catalog.application.dto.common.ProductStockData
import dev.cnpe.ventescabekotlin.catalog.application.dto.response.ProductCreatedResponse
import dev.cnpe.ventescabekotlin.catalog.application.dto.response.ProductDetailedResponse
import dev.cnpe.ventescabekotlin.catalog.application.dto.response.ProductPriceInfoResponse
import dev.cnpe.ventescabekotlin.catalog.application.dto.response.ProductSummaryResponse
import dev.cnpe.ventescabekotlin.catalog.domain.model.Product
import dev.cnpe.ventescabekotlin.catalog.domain.model.ProductPrice
import dev.cnpe.ventescabekotlin.inventory.application.api.dto.ProductInventorySummary
import dev.cnpe.ventescabekotlin.shared.application.dto.ResourceAuditData
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ProductMapper {

    /**
     * Maps a Product entity to a ProductCreatedResponse DTO.
     */
    fun toCreated(product: Product): ProductCreatedResponse {
        return ProductCreatedResponse(
            id = product.id!!,
            status = product.status,
            createdAt = product.createdAt
        )
    }

    /**
     * Maps a Product entity to a ProductSummaryResponse DTO.
     */
    fun toSummary(product: Product): ProductSummaryResponse {
        val currentPrice = product.getCurrentPrice()
        val sellingPrice = currentPrice?.sellingPrice?.amount ?: BigDecimal.ZERO

        return ProductSummaryResponse(
            id = product.id!!,
            name = product.name,
            barcode = product.barcode,
            currentSellingPrice = sellingPrice,
            currentTotalStock = product.totalCurrentStock,
            status = product.status,
            categoryId = product.categoryId
        )
    }

    /**
     * Maps a Product entity and related info (pricing, inventory) to a ProductDetailedResponse DTO.
     * Assumes pricingData and inventoryItemInfo are pre-calculated/fetched and passed in.
     */
    fun toDetailed(
        product: Product,
        pricingData: ProductPricingData,
        stockData: ProductStockData
    ): ProductDetailedResponse {
        return ProductDetailedResponse(
            id = product.id!!,
            details = ProductDetails(
                name = product.name,
                sku = product.sku,
                barcode = product.barcode,
                description = product.description,
                status = product.status
            ),
            pricing = pricingData,
            stockInfo = stockData,
            photos = product.photos,
            relations = ProductRelationsData(
                categoryId = product.categoryId,
                brandId = product.brandId,
                supplierId = product.supplierId
            ),
            auditData = ResourceAuditData.fromBaseEntity(product)
        )
    }

    /**
     * Maps a ProductPrice entity (historical price record) to a ProductPriceInfoResponse DTO.
     */
    fun toProductPriceInfo(productPrice: ProductPrice): ProductPriceInfoResponse {
        return ProductPriceInfoResponse(
            sellingPrice = productPrice.sellingPrice.amount,
            supplierCost = productPrice.supplierCost.amount,
            profit = productPrice.calculateProfit().amount,
            profitMargin = productPrice.calculateProfitMargin(),
            startDate = productPrice.startDate,
            endDate = productPrice.endDate,
            changeReason = productPrice.reason.name
        )
    }
}