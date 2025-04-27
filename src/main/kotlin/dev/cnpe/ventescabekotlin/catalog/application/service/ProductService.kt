package dev.cnpe.ventescabekotlin.catalog.application.service

import dev.cnpe.ventescabekotlin.brands.application.api.BrandInfoPort
import dev.cnpe.ventescabekotlin.business.application.api.BusinessDataPort
import dev.cnpe.ventescabekotlin.catalog.application.dto.common.ProductPricingData
import dev.cnpe.ventescabekotlin.catalog.application.dto.common.ProductStockData
import dev.cnpe.ventescabekotlin.catalog.application.dto.request.CreateProductDraftRequest
import dev.cnpe.ventescabekotlin.catalog.application.dto.request.CreateProductRequest
import dev.cnpe.ventescabekotlin.catalog.application.dto.response.ProductCreatedResponse
import dev.cnpe.ventescabekotlin.catalog.application.dto.response.ProductDetailedResponse
import dev.cnpe.ventescabekotlin.catalog.application.dto.response.ProductPriceInfoResponse
import dev.cnpe.ventescabekotlin.catalog.application.dto.response.ProductSummaryResponse
import dev.cnpe.ventescabekotlin.catalog.application.factory.ProductFactory
import dev.cnpe.ventescabekotlin.catalog.application.mapper.ProductMapper
import dev.cnpe.ventescabekotlin.catalog.application.util.ProductUtils
import dev.cnpe.ventescabekotlin.catalog.application.validation.ProductRelationValidator
import dev.cnpe.ventescabekotlin.catalog.event.ProductCreatedEvent
import dev.cnpe.ventescabekotlin.catalog.infrastructure.persistence.ProductPriceRepository
import dev.cnpe.ventescabekotlin.catalog.infrastructure.persistence.ProductRepository
import dev.cnpe.ventescabekotlin.categories.application.api.CategoryInfoPort
import dev.cnpe.ventescabekotlin.inventory.application.api.InventoryInfoPort
import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockUnitType
import dev.cnpe.ventescabekotlin.shared.application.dto.PageResponse
import dev.cnpe.ventescabekotlin.shared.application.exception.DomainException
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode.INVALID_STATE
import dev.cnpe.ventescabekotlin.shared.application.exception.createDuplicatedResourceException
import dev.cnpe.ventescabekotlin.shared.application.exception.createResourceNotFoundException
import dev.cnpe.ventescabekotlin.suppliers.application.api.SupplierInfoPort
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
    private val productPriceRepository: ProductPriceRepository,
    private val productFactory: ProductFactory,
    private val productMapper: ProductMapper,
    private val businessDataPort: BusinessDataPort,
    private val productUtils: ProductUtils,
    private val eventPublisher: ApplicationEventPublisher,
    private val inventoryInfoPort: InventoryInfoPort,
    private val categoryInfoPort: CategoryInfoPort,
    private val brandInfoPort: BrandInfoPort,
    private val supplierInfoPort: SupplierInfoPort,
    private val relationValidator: ProductRelationValidator
) {

    fun createProductDraft(request: CreateProductDraftRequest): ProductCreatedResponse {
        log.debug { "Creating product draft: Name=${request.name}, Barcode=${request.barcode}" }
        validateProductDraftRequest(request)

        val currencyCode = businessDataPort.getBusinessPaymentData().currencyCode
        val defaultCategoryId = categoryInfoPort.getDefaultCategoryId()
        val defaultBrandId = brandInfoPort.getDefaultBrandId()
        val defaultSupplierId = supplierInfoPort.getDefaultSupplierId()

        val newProductDraft = productFactory.createDraft(
            barcode = request.barcode,
            productName = request.name,
            defaultCategoryId = defaultCategoryId,
            defaultBrandId = defaultBrandId,
            defaultSupplierId = defaultSupplierId,
            currencyCode = currencyCode
        )

        val savedProduct = productRepository.save(newProductDraft)
        log.info { "Product draft created: ${savedProduct.name} (ID: ${savedProduct.id})" }

        eventPublisher.publishEvent(ProductCreatedEvent(savedProduct.id!!, StockUnitType.UNIT))

        return productMapper.toCreated(savedProduct)
    }

    fun createProduct(request: CreateProductRequest): ProductCreatedResponse {
        log.debug { "Creating full product: Name=${request.name}, Barcode=${request.barcode}" }
        validateProductRequest(request)

        val currencyCode = businessDataPort.getBusinessPaymentData().currencyCode

        val newProduct = productFactory.createFromRequest(request, currencyCode)

        val savedProduct = productRepository.save(newProduct)
        log.info { "Full product created: ${savedProduct.name} (ID: ${savedProduct.id})" }

        eventPublisher.publishEvent(ProductCreatedEvent(savedProduct.id!!, request.stockUnitType))

        return productMapper.toCreated(savedProduct)
    }

    @Transactional(readOnly = true)
    fun getProductDetails(id: Long): ProductDetailedResponse {
        log.debug { "Fetching details for product ID: $id" }

        val product = productRepository.findByIdOrNull(id)
            ?: throw createResourceNotFoundException("Product", id)

        val inventorySummary = inventoryInfoPort.getInventorySummary(id)
        val businessPaymentData = businessDataPort.getBusinessPaymentData()

        val currentPrice = product.getCurrentPrice()
            ?: throw DomainException(INVALID_STATE, details = mapOf("reason" to "NO_ACTIVE_PRICE"))

        val pricingData = ProductPricingData(
            currency = businessPaymentData.currencyCode,
            currentNetSellingPrice = productUtils.calculateNetPrice(
                currentPrice.sellingPrice.amount,
                businessPaymentData
            ),
            currentSellingPrice = currentPrice.sellingPrice.amount,
            currentSupplierCost = currentPrice.supplierCost.amount,
            currentProfit = currentPrice.calculateProfit().amount,
            currentProfitMargin = currentPrice.calculateProfitMargin()
        )

        val productStockData = ProductStockData(
            currentQuantity = inventorySummary.totalStockQuantity,
            minimumQuantity = inventorySummary.representativeMinimumQuantity,
            unitOfMeasure = inventorySummary.unitOfMeasure
        )

        return productMapper.toDetailed(product, pricingData, productStockData)
    }

    @Transactional(readOnly = true)
    fun getAllProductSummaries(pageable: Pageable): PageResponse<ProductSummaryResponse> {
        log.debug { "Fetching product summaries page: $pageable" }
        val productPage = productRepository.findAllWithPriceHistory(pageable)

        return PageResponse.from(productPage.map { productMapper.toSummary(it) })

    }

    @Transactional(readOnly = true)
    fun getProductPriceHistory(productId: Long, pageable: Pageable): PageResponse<ProductPriceInfoResponse> {
        log.debug { "Fetching price history for product ID: $productId, page: $pageable" }
        if (!productRepository.existsById(productId)) {
            throw createResourceNotFoundException("Product", productId)
        }
        val pricePage = productPriceRepository.getProductPriceHistory(productId, pageable)
        return PageResponse.from(pricePage.map { productMapper.toProductPriceInfo(it) })
    }

    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun validateProductDraftRequest(request: CreateProductDraftRequest) {
        if (productRepository.existsByName(request.name)) {
            throw createDuplicatedResourceException("name", request.name)
        }
        if (productRepository.existsByBarcode(request.barcode)) {
            throw createDuplicatedResourceException("barcode", request.barcode)
        }
    }

    private fun validateProductRequest(request: CreateProductRequest) {
        if (productRepository.existsByName(request.name)) {
            throw createDuplicatedResourceException("name", request.name)
        }
        if (productRepository.existsByBarcode(request.barcode)) {
            throw createDuplicatedResourceException("barcode", request.barcode)
        }
        relationValidator.validateRelations(
            categoryId = request.categoryId,
            brandId = request.brandId,
            supplierId = request.supplierId
        )
    }
}