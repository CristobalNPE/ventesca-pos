package dev.cnpe.ventescaposbe.orders.application.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import dev.cnpe.ventescaposbe.currency.vo.Money
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(description = "A single line item detailing a product sold on the receipt.")
data class ReceiptLineItem(

    @Schema(description = "Quantity of the product sold.")
    val quantity: Double,

    @Schema(description = "Name of the product.")
    val productName: String,

    @Schema(description = "Stock Keeping Unit (SKU) of the product, if available.")
    val sku: String?,

    @Schema(description = "Price per unit (gross, including tax).")
    val unitPrice: Money,

    @Schema(description = "Total price for this line (quantity * unitPrice, gross).")
    val lineTotal: Money,

    @Schema(description = "Discount amount applied specifically to this line item, if any.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val discountApplied: Money?
)