package dev.cnpe.ventescaposbe.orders.application.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Structured data for generating an order receipt.")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReceiptResponse(

    val header: ReceiptHeader,

    val customerInfo: ReceiptCustomerInfo?,

    val items: List<ReceiptLineItem>,

    val totals: ReceiptTotals,

    val payments: List<ReceiptPayment>,

    val footer: ReceiptFooter?
)
