package dev.cnpe.ventescaposbe.orders.application.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.domain.enums.OrderStatus
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Detailed view of an order.")
data class OrderResponse(

    @Schema(description = "Unique ID of the order.")
    val id: Long,

    @Schema(description = "System-generated order number.")
    val orderNumber: String,

    @Schema(description = "Current status of the order.")
    val status: OrderStatus,

    @Schema(description = "ID of the branch where the order was placed.")
    val branchId: Long,

    @Schema(description = "ID of the user who processed the order.")
    val userIdpId: String,

    @Schema(description = "ID of the associated customer, if any.")
    val customerId: Long?,

    @Schema(description = "Timestamp when the order was created/placed.")
    val orderTimestamp: OffsetDateTime,

    @Schema(description = "List of items included in the order.")
    val items: List<OrderItemResponse>,

    @Schema(description = "List of payments applied to the order.")
    val payments: List<PaymentResponse>,

    @Schema(description = "Total net amount of all items before tax and discounts.")
    val subTotal: Money,

    @Schema(description = "Total tax amount calculated for the order.")
    val taxAmount: Money,

    @Schema(description = "Total gross amount (subTotal + taxAmount).")
    val totalAmount: Money,

    @Schema(description = "Total discount applied to the order (sum of item discounts).")
    val discountAmount: Money,

    @Schema(description = "The final amount due or paid (totalAmount - discountAmount).")
    val finalAmount: Money,

    @Schema(description = "Total amount confirmed as paid.")
    val totalPaid: Money,

    @Schema(description = "Amount remaining to be paid.")
    val amountDue: Money,

    @Schema(description = "Change due to the customer if overpaid (null otherwise).")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val changeDue: Money?,

    @Schema(description = "Optional notes added to the order.")
    val notes: String?,

    @Schema(description = "Audit data associated with the order.")
    val auditData: ResourceAuditData
)