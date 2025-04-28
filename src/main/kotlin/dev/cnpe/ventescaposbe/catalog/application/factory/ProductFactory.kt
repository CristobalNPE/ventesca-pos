package dev.cnpe.ventescaposbe.catalog.application.factory

import dev.cnpe.ventescaposbe.catalog.application.dto.request.CreateProductRequest
import dev.cnpe.ventescaposbe.catalog.application.service.SkuGenerator
import dev.cnpe.ventescaposbe.catalog.domain.enums.PriceChangeReason
import dev.cnpe.ventescaposbe.catalog.domain.enums.ProductStatus
import dev.cnpe.ventescaposbe.catalog.domain.model.Product
import dev.cnpe.ventescaposbe.catalog.domain.model.ProductPrice
import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import org.springframework.stereotype.Component

/**
 * Factory responsible for constructing Product domain entities and initial ProductPrice.
 * Relies on calling services to provide necessary context like currency and relationship IDs.
 */
@Component
class ProductFactory(
    private val moneyFactory: MoneyFactory,
    private val skuGenerator: SkuGenerator
) {


    /**
     * Creates a new Product entity in a DRAFT state with minimal information.
     * Requires default relationship IDs to be provided by the calling service.
     */
    fun createDraft(
        barcode: String,
        productName: String,
        defaultCategoryId: Long,
        defaultBrandId: Long,
        defaultSupplierId: Long,
        currencyCode: String
    ): Product {
        require(barcode.isNotBlank()) { "Barcode cannot be blank" }
        require(productName.isNotBlank()) { "Product name cannot be blank" }

        val sanitizedProductName = sanitizeProductName(productName)

        val product = Product(
            name = sanitizedProductName,
            barcode = barcode,
            status = ProductStatus.DRAFT,
            categoryId = defaultCategoryId,
            brandId = defaultBrandId,
            supplierId = defaultSupplierId,
        )

        product.assignSku(skuGenerator.generateSku(product.name))

        val zeroPrice = moneyFactory.zero(currencyCode)
        val initialPrice = ProductPrice(
            sellingPrice = zeroPrice,
            supplierCost = zeroPrice,
            reason = PriceChangeReason.INITIAL,
            product = product
        )
        product.addPrice(initialPrice)
        return product
    }


    /**
     * Creates a fully populated Product entity from a DTO request.
     * Requires currency code to be provided by the calling service.
     */
    fun createFromRequest(request: CreateProductRequest, currencyCode: String): Product {
        val product = Product(
            name = sanitizeProductName(request.name),
            barcode = request.barcode,
            description = request.description?.trim(),
            status = request.status,
            categoryId = request.categoryId,
            brandId = request.brandId,
            supplierId = request.supplierId,
            photos = request.photos?.toMutableList() ?: mutableListOf()
        )

        product.assignSku(skuGenerator.generateSku(product.name))

        val sellingPriceMoney = moneyFactory.createMoney(request.sellingPrice, currencyCode)
        val supplierCostMoney = moneyFactory.createMoney(request.supplierCost, currencyCode)

        val initialPrice = ProductPrice(
            sellingPrice = sellingPriceMoney,
            supplierCost = supplierCostMoney,
            reason = PriceChangeReason.INITIAL,
            product = product
        )
        product.addPrice(initialPrice)

        // TODO: Consider stock unit type - where should this be stored/handled?

        return product
    }

    /**
     * Sanitizes the product name by trimming whitespace, converting it to lowercase,
     * and capitalizing the first character.
     *
     * @param productName The original product name to be sanitized.
     * @return A sanitized version of the product name.
     */
    private fun sanitizeProductName(productName: String): String {
        return productName.trim().lowercase().replaceFirstChar { it.titlecase() }
    }
}