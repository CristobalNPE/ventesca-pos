package dev.cnpe.ventescaposbe.promotions.application.dto.response

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountApplicability
import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountRuleType
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.OffsetDateTime

@Schema(description = "Detailed information about a discount/promotion rule.")
data class DiscountRuleResponse(

    @Schema(description = "Unique ID of the rule.")
    val id: Long,

    @Schema(description = "Unique, descriptive name for the rule.")
    val name: String,

    @Schema(description = "Optional longer description.")
    val description: String?,

    @Schema(description = "The type of discount.")
    val type: DiscountRuleType,

    @Schema(description = "The value of the discount (% or fixed amount).")
    val value: BigDecimal,

    @Schema(description = "Optional start date/time (ISO 8601).")
    val startDate: OffsetDateTime? = null,

    @Schema(description = "Optional end date/time (ISO 8601).")
    val endDate: OffsetDateTime? = null,

    @Schema(description = "Whether the rule is currently active.")
    val isActive: Boolean = true,

    @Schema(description = "Specifies what the discount applies to.")
    val applicability: DiscountApplicability = DiscountApplicability.ORDER_TOTAL,

    @Schema(description = "Set of applicable Product IDs.")
    val targetProductIds: Set<Long>?,

    @Schema(description = "Set of applicable Category IDs.")
    val targetCategoryIds: Set<Long>?,

    @Schema(description = "Set of applicable Brand IDs.")
    val targetBrandIds: Set<Long>?,

    @Schema(description = "Optional minimum quantity required.")
    val minimumQuantity: Int? = null,

    @Schema(description = "Optional minimum spend required.")
    val minimumSpend: Money? = null,

    @Schema(description = "Can this discount be combined with others?")
    val isCombinable: Boolean = false,

    @Schema(description = "Audit information for the rule.")
    val auditData: ResourceAuditData
)