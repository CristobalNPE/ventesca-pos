package dev.cnpe.ventescaposbe.catalog.domain.model

import dev.cnpe.ventescaposbe.catalog.domain.enums.ProductStatus
import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode.INVALID_STATE
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import dev.cnpe.ventescaposbe.shared.domain.vo.Image
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "products",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_product_sku", columnNames = ["sku"]),
        UniqueConstraint(name = "uk_product_barcode", columnNames = ["barcode"])
    ],
    indexes = [
        Index(name = "idx_product_sku", columnList = "sku"),
        Index(name = "idx_product_barcode", columnList = "barcode")
    ]
)
class Product(

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "sku", unique = true)
    var sku: String? = null,

    @Column(name = "barcode", unique = true, updatable = false)
    val barcode: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ProductStatus = ProductStatus.DRAFT,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_images", joinColumns = [JoinColumn(name = "product_id")])
    @OrderColumn(name = "image_order")
    var photos: MutableList<Image> = mutableListOf(),

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "total_current_stock")
    var totalCurrentStock: Double = 0.0,

    @Column(name = "category_id", nullable = false)
    var categoryId: Long?,

    @Column(name = "brand_id", nullable = false)
    var brandId: Long?,

    @Column(name = "supplier_id", nullable = false)
    var supplierId: Long?,

    @OneToMany(
        mappedBy = "product",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @OrderBy("startDate DESC")
    val priceHistory: MutableSet<ProductPrice> = mutableSetOf(),


    id: Long? = null,
    version: Int = 0,
) : BaseEntity(id, version) {

    /**
     * Assigns the SKU if not already set. Prevents reassignment.
     * @throws DomainException if SKU is already assigned.
     */
    fun assignSku(skuToAssign: String) {
        require(skuToAssign.isNotBlank()) { "SKU cannot be blank" }
        if (this.sku != null) {
            throw DomainException(
                INVALID_STATE,
                details = mapOf("property" to "sku", "value" to skuToAssign, "reason" to "ALREADY_ASSIGNED"),
            )
        }
        this.sku = skuToAssign
    }

    /**
     * Adds a new price record to the product's history.
     * Automatically sets the end date of the previous current price.
     * @param price The new ProductPrice record (should have product link potentially unset).
     * @throws DomainException if the new price is invalid.
     */
    fun addPrice(price: ProductPrice) {
        price.validatePrices()

        getCurrentPrice()?.endDate = LocalDateTime.now()

        price.product = this
        this.priceHistory.add(price)
    }

    /**
     * Retrieves the currently active price record, if one exists.
     * Returns null if no price is currently active.
     */
    fun getCurrentPrice(): ProductPrice? {
        // Assumes @OrderBy("startDate DESC") keeps the latest start dates first
        return priceHistory.firstOrNull { it.isActive() }
    }

    /**
     * Validates if the product has the minimum required data to be activated.
     * Checks for positive prices, matching currencies, and assigned relationships.
     * @throws DomainException if validation fails.
     */
    fun validateCanActivate() {
        val currentPrice = getCurrentPrice()
            ?: throw DomainException(
                INVALID_STATE,
                details = mapOf("reason" to "MISSING_ACTIVE_PRICE"),
            )

        require(currentPrice.sellingPrice.isPositive()) { "Selling price must be positive for activation." }
        require(currentPrice.supplierCost.isPositive()) { "Supplier cost must be positive for activation." }

        require(categoryId != null) { "Product must have a category assigned for activation." }
        require(brandId != null) { "Product must have a brand assigned for activation." }
        require(supplierId != null) { "Product must have a supplier assigned for activation." }
        require(!sku.isNullOrBlank()) { "Product must have an SKU assigned for activation." }
    }

    /** Activates the product if validation passes. */
    fun activate() {
        validateCanActivate()
        this.status = ProductStatus.ACTIVE
    }

    /** Deactivates the product. */
    fun deactivate() {
        this.status = ProductStatus.INACTIVE
    }

}