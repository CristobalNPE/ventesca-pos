package dev.cnpe.ventescaposbe.promotions.application.api.dto

import dev.cnpe.ventescaposbe.currency.vo.Money
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Contextual information about a single item within an order, used for discount evaluation.")
data class ItemContextData(

    @Schema(description = "ID of the product.")
    val productId: Long,

    @Schema(description = "ID of the product's category (if known).")
    val categoryId: Long?,

    @Schema(description = "ID of the product's brand (if known).")
    val brandId: Long?,

    @Schema(description = "Quantity of this item in the order.")
    val quantity: Double,

    @Schema(description = "The original unit price of the item (Gross - tax included, before any discounts).")
    val originalUnitPrice: Money,

    @Schema(description = "The current total price for this line item (originalUnitPrice * quantity).")
    val currentItemTotalPrice: Money
)