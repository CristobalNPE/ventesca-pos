package dev.cnpe.ventescaposbe.reporting.application.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import dev.cnpe.ventescaposbe.currency.vo.Money
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Summary of sales performance over a period.")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SalesSummaryReport(

    @Schema(description = "Total number of completed orders in the period.")
    val totalOrders: Int,

    @Schema(description = "Total revenue generated (sum of order final amounts).")
    val totalRevenue: Money,


    @Schema(description = "Total tax amount collected.")
    val totalTax: Money,

    @Schema(description = "Total discount amount given (sum of all order/item discounts).")
    val totalDiscount: Money,

    @Schema(description = "Average value per completed order (totalRevenue / totalOrders). Null if no orders.")
    val averageOrderValue: Money?
)