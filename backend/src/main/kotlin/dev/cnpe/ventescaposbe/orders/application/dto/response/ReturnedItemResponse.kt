package dev.cnpe.ventescaposbe.orders.application.dto.response

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.domain.enums.ReturnReason
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Details of a specific item included in a return transaction.")
data class ReturnedItemResponse(

    @Schema(description = "Unique ID of the returned item record.")
    val id: Long,

    @Schema(description = "ID of the original order item this return corresponds to.")
    val originalOrderItemId: Long,

    @Schema(description = "ID of the product returned.")
    val productId: Long,

    @Schema(description = "Name of the product at the time of return.")
    val productName: String,

    @Schema(description = "SKU of the product at the time of return.")
    val sku: String?,

    @Schema(description = "Quantity of this item returned.")
    val quantityReturned: Double,

    @Schema(description = "The refund amount per unit for this item.")
    val unitRefundAmount: Money,

    @Schema(description = "Total refund amount for this line item.")
    val totalItemRefundAmount: Money,

    @Schema(description = "Reason for the return.")
    val reason: ReturnReason,

    @Schema(description = "Indicates if the item was designated to be restocked.")
    val restock: Boolean
)
