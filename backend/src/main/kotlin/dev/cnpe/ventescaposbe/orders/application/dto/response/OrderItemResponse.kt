package dev.cnpe.ventescaposbe.orders.application.dto.response

import dev.cnpe.ventescaposbe.currency.vo.Money
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Details of an item within an order.")
data class OrderItemResponse(

    @Schema(description = "Unique ID of the order item record.")
    val id: Long,

    @Schema(description = "ID of the product.")
    val productId: Long,

    @Schema(description = "Product name at the time of sale.")
    val productName: String,

    @Schema(description = "Product SKU at the time of sale.")
    val sku: String?,

    @Schema(description = "Quantity ordered.")
    val quantity: Double,

    @Schema(description = "Price per unit (tax included).")
    val unitPrice: Money,

    @Schema(description = "Net price per unit (tax excluded).")
    val netUnitPrice: Money,

    @Schema(description = "Total gross price for this line (unitPrice * quantity).")
    val totalPrice: Money,

    @Schema(description = "Total net price for this line (netUnitPrice * quantity).")
    val totalNetPrice: Money,

    @Schema(description = "Discount applied to this specific line item.")
    val discountAmount: Money,

    @Schema(description = "Final price for this line item (totalPrice - discountAmount).")
    val finalPrice: Money
)