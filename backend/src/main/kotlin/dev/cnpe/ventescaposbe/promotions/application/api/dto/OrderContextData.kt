package dev.cnpe.ventescaposbe.promotions.application.api.dto

import dev.cnpe.ventescaposbe.currency.vo.Money
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Contextual information about the entire order, used for discount evaluation.")
data class OrderContextData(

    @Schema(description = "ID of the branch where the order is taking place.")
    val branchId: Long,

    @Schema(description = "ID of the customer associated with the order (if any).")
    val customerId: Long?,

    @Schema(description = "List providing context for each item currently in the order.")
    val currentItems: List<ItemContextData>,

    @Schema(description = "The current calculated total amount of the order (sum of item gross prices) before applying any order-level discounts.")
    val currentTotalAmountPreDiscount: Money // should be the total --before the order-level discount is applied.
)