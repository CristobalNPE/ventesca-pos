package dev.cnpe.ventescaposbe.inventory.domain.vo

import dev.cnpe.ventescaposbe.inventory.domain.enums.StockUnitType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Schema(description = "Represents stock quantity, minimum level, and unit of measure.")
@Embeddable
data class Stock(

    @Schema(description = "Current quantity in stock.", example = "10.0")
    @Column(name = "quantity", nullable = false)
    val quantity: Double,

    @Schema(description = "Minimum desired stock level.", example = "5.0")
    @Column(name = "minimum_quantity", nullable = false)
    val minimumQuantity: Double,

    @Schema(description = "Unit of measure for the stock.", example = "UNIT")
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false)
    val unit: StockUnitType
) {
    init {
        validate()
    }

    companion object {
        private const val NEEDS_RESTOCK_THRESHOLD_FACTOR = 1.5

        fun withDefaults(): Stock = Stock(0.0, 0.0, StockUnitType.UNIT)
        fun withDefaultsFor(unitType: StockUnitType): Stock = Stock(0.0, 0.0, unitType)
    }

    private fun validate() {
        require(quantity >= 0) { "Stock quantity cannot be negative" }
        require(minimumQuantity >= 0) { "Minimum stock level cannot be negative" }
    }

    @get:Schema(description = "True if current quantity is at or below the minimum level.")
    val isLowStock: Boolean
        get() = quantity <= minimumQuantity

    @get:Schema(description = "True if current quantity is exactly zero.")
    val isOutOfStock: Boolean
        get() = quantity == 0.0

    @get:Schema(description = "Calculated available quantity (current - minimum, minimum 0).")
    val availableQuantity: Double
        get() = (quantity - minimumQuantity).coerceAtLeast(0.0)

    @get:Schema(description = "True if current quantity is below 1.5 times the minimum level.")
    val needsRestock: Boolean
        get() = minimumQuantity > 0 && quantity < minimumQuantity * NEEDS_RESTOCK_THRESHOLD_FACTOR


    /** Returns a new Stock instance with the added quantity. */
    fun addQuantity(amount: Double): Stock {
        require(amount >= 0) { "Cannot add negative quantity" }
        return this.copy(quantity = this.quantity + amount)
    }

    /** Returns a new Stock instance with the removed quantity. */
    fun removeQuantity(amount: Double): Stock {
        require(amount >= 0) { "Cannot remove negative quantity" }
        require(amount <= quantity) { "Insufficient stock to remove $amount (available: $quantity)" }
        return this.copy(quantity = this.quantity - amount)
    }

    /** Returns a new Stock instance with the updated minimum quantity. */
    fun updateMinimumQuantity(newMinimum: Double): Stock {
        require(newMinimum >= 0) { "Minimum quantity cannot be negative" }
        return this.copy(minimumQuantity = newMinimum)
    }

    /** Returns a new Stock instance with the changed unit of measure. */
    fun changeUnit(newUnit: StockUnitType): Stock {
        return this.copy(unit = newUnit)
    }

    /** Checks if the current stock can fulfill the requested order quantity. */
    fun canFulfillOrder(orderQuantity: Double): Boolean {
        require(orderQuantity > 0) { "Order quantity must be positive" }
        return orderQuantity <= quantity
    }

    /**
     * Calculates the current stock level as a percentage of a target quantity.
     * Returns 0.0 if targetQuantity is null or non-positive. Caps at 100.0.
     */
    fun getStockPercentage(targetQuantity: Double?): Double {
        if (targetQuantity == null || targetQuantity <= 0) {
            return 0.0
        }
        val percentage = (quantity / targetQuantity) * 100.0
        return percentage.coerceIn(0.0, 100.0)
    }
}
