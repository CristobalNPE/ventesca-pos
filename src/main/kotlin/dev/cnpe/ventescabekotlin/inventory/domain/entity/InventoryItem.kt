package dev.cnpe.ventescabekotlin.inventory.domain.entity

import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockUnitType
import dev.cnpe.ventescabekotlin.inventory.domain.vo.Stock
import dev.cnpe.ventescabekotlin.shared.domain.model.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "inventory_items",
    indexes = [
        Index(name = "idx_invitem_product_branch", columnList = "product_id, branch_id", unique = true),
        Index(name = "idx_invitem_product_id", columnList = "product_id"),
        Index(name = "idx_invitem_branch_id", columnList = "branch_id")
    ]
)
class InventoryItem(
    @Column(name = "branch_id", nullable = false, updatable = false)
    val branchId: Long,

    @Column(name = "product_id", nullable = false, updatable = false)
    val productId: Long,


    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "quantity", column = Column(name = "stock_quantity")),
        AttributeOverride(name = "minimumQuantity", column = Column(name = "minimum_stock_level")),
        AttributeOverride(name = "unit", column = Column(name = "unit_of_measure"))
    )
    var stock: Stock,

    @OneToMany(mappedBy = "item", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val stockModifications: MutableSet<StockModification> = mutableSetOf(),

    id: Long? = null,
    version: Int = 0
) : BaseEntity(id, version) {

    companion object {
        /**
         * Factory method to create an InventoryItem for a newly created product in a specific branch.
         * Initializes stock with defaults based on the product's unit type.
         */
        fun forNewProduct(productId: Long, branchId: Long, unitType: StockUnitType): InventoryItem {
            require(productId > 0) { "Product ID must be positive" }
            require(branchId > 0) { "Branch ID must be positive" }

            return InventoryItem(
                branchId = branchId,
                productId = productId,
                stock = Stock.withDefaultsFor(unitType),
                stockModifications = mutableSetOf()
            )
        }
    }

    fun addStockModification(modification: StockModification) {
        requireNotNull(modification) { "Stock modification cannot be null" }
        modification.item = this
        stockModifications.add(modification)
    }
    val currentQuantity: Double
        get() = stock.quantity

}