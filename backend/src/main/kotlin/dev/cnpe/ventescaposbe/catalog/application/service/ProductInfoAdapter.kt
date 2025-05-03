package dev.cnpe.ventescaposbe.catalog.application.service

import dev.cnpe.ventescaposbe.catalog.api.ProductInfoPort
import dev.cnpe.ventescaposbe.catalog.api.dto.ProductSaleInfo
import dev.cnpe.ventescaposbe.catalog.infrastructure.persistence.ProductRepository
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class ProductInfoAdapter(
    private val productRepository: ProductRepository
) : ProductInfoPort {

    override fun countProductsBySupplierId(supplierId: Long): Long {
        log.debug { "Counting products for Supplier ID: $supplierId" }
        val count = productRepository.countAllBySupplierId(supplierId)
        log.debug { "Found $count products for Supplier ID: $supplierId" }
        return count
    }

    override fun countProductsByBrandId(brandId: Long): Long {
        log.debug { "Counting products for Brand ID: $brandId" }
        val count = productRepository.countAllByBrandId(brandId)
        log.debug { "Found $count products for Brand ID: $brandId" }
        return count
    }

    override fun countProductsByCategoryId(categoryId: Long): Long {
        log.debug { "Counting products for Category ID: $categoryId" }
        val count = productRepository.countAllByCategoryId(categoryId)
        log.debug { "Found $count products for Category ID: $categoryId" }
        return count
    }

    override fun getProductSaleInfo(productId: Long): ProductSaleInfo {
        log.debug { "Retrieving product sale info for ID: $productId" }
        val product = productRepository.findByIdOrNull(productId)
            ?: run {
                log.debug { "No product found for ID: $productId" }
                throw createResourceNotFoundException("Product", productId)
            }
        return ProductSaleInfo(
            productId = product.id!!,
            status = product.status,
            categoryId = product.categoryId,
            brandId = product.brandId,
            supplierId = product.supplierId,
            currentSellingPrice = product.getCurrentPrice()?.sellingPrice,
            productName = product.name,
            productSku = product.sku
        )
    }

}