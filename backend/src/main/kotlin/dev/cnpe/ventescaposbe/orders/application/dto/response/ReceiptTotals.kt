package dev.cnpe.ventescaposbe.orders.application.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import dev.cnpe.ventescaposbe.currency.vo.Money
import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(description = "Summary totals for the receipt.")
data class ReceiptTotals(

    @Schema(description = "Total value of items before tax and discounts (net).")
    val subTotal: Money,

    @Schema(description = "Total amount of tax calculated for the order.")
    val taxAmount: Money,

    @Schema(description = "Total value of items including tax, before discounts (gross).")
    val grossTotal: Money,

    @Schema(description = "Total discount amount applied to the entire order (sum of item and order discounts).")
    val discountAmount: Money,

    @Schema(description = "The final amount payable by the customer after all discounts.")
    val finalAmount: Money,

    @Schema(description = "The total amount confirmed as paid by the customer.")
    val totalPaid: Money,

    @Schema(description = "Change due back to the customer, if any.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val changeDue: Money?
)