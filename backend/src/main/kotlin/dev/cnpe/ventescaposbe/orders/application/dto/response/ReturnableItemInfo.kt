package dev.cnpe.ventescaposbe.orders.application.dto.response

import dev.cnpe.ventescaposbe.currency.vo.Money
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Information about an item from a completed order that is eligible for return.")
data class ReturnableItemInfo(

    @Schema(description = "ID of the original order item record.")
    val originalOrderItemId: Long,

    @Schema(description = "ID of the product.")
    val productId: Long,

    @Schema(description = "Name of the product.")
    val productName: String,

    @Schema(description = "SKU of the product.")
    val sku: String?,

    @Schema(description = "Quantity originally purchased in the order item.")
    val originalQuantity: Double,

    @Schema(description = "Quantity already returned from this order item in previous returns.")
    val alreadyReturnedQuantity: Double,

    @Schema(description = "Remaining quantity eligible for return.")
    val returnableQuantity: Double,

    @Schema(description = "The final price paid per unit for this item in the original order.")
    val finalUnitPricePaid: Money
)