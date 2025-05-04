package dev.cnpe.ventescaposbe.customers.application.dto.response

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import dev.cnpe.ventescaposbe.shared.domain.vo.Address
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Detailed information about a customer.")
data class CustomerDetailedResponse(

    @Schema(description = "Unique ID of the customer.")
    val id: Long,

    @Schema(description = "Customer's first name.")
    val firstName: String,
    @Schema(description = "Customer's last name, if available.")
    val lastName: String?,
    @Schema(description = "Customer's Tax ID / Personal ID, if available.")
    val taxId: String?,

    @Schema(description = "Customer's email address, if available.")
    val email: String?,
    @Schema(description = "Customer's phone number, if available.")
    val phone: String?,

    @Schema(description = "Customer's address, if available.")
    val address: Address?,

    @Schema(description = "Total amount spent by the customer.")
    val totalSpent: Money,
    @Schema(description = "Total number of orders placed by the customer.")
    val totalOrders: Int,
    @Schema(description = "Timestamp of the customer's last order, if any.")
    val lastOrderDate: OffsetDateTime?,

    @Schema(description = "Notes about the customer.")
    val notes: String?,
    @Schema(description = "Indicates if the customer record is active.")
    val isActive: Boolean,

    @Schema(description = "Audit information.")
    val auditData: ResourceAuditData
)