package dev.cnpe.ventescaposbe.promotions.application.dto.request

import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountApplicability
import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountRuleType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.OffsetDateTime

@Schema(description = "Request payload for creating a new discount/promotion rule.")
data class CreateDiscountRuleRequest(

    @field:NotBlank(message = "Rule name cannot be blank.")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters.")
    @Schema(
        description = "Unique, descriptive name for the rule.",
        example = "10% Off All Drinks",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val name: String,

    @field:Size(max = 255, message = "Description cannot exceed 255 characters.")
    @Schema(
        description = "Optional longer description of the rule.",
        example = "Applies 10% discount to all items in the Drinks category."
    )
    val description: String?,

    @field:NotNull(message = "Discount type must be provided.")
    @Schema(
        description = "The type of discount.",
        example = "ITEM_PERCENTAGE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val type: DiscountRuleType,

    @field:NotNull(message = "Discount value must be provided.")
    @field:DecimalMin(value = "0.0", message = "Discount value cannot be negative.")
    @Schema(
        description = "The value of the discount (e.g., 10.0 for 10% or 5.00 for $5).",
        example = "10.0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val value: BigDecimal,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(
        description = "Optional start date/time (ISO 8601) when the rule becomes active.",
        example = "2024-08-01T00:00:00Z"
    )
    val startDate: OffsetDateTime? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Optional end date/time (ISO 8601) when the rule expires.", example = "2024-08-31T23:59:59Z")
    val endDate: OffsetDateTime? = null,

    @field:NotNull(message = "Active status must be provided.")
    @Schema(
        description = "Whether the rule is currently active.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isActive: Boolean = true,

    // *******************************
    // 🔰 Applicability & Conditions
    // *******************************

    @field:NotNull(message = "Applicability must be provided.")
    @Schema(
        description = "Specifies what the discount applies to.",
        example = "CATEGORIES",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val applicability: DiscountApplicability = DiscountApplicability.ORDER_TOTAL,

    @Schema(description = "Set of Product IDs this rule applies to (used if applicability is SPECIFIC_PRODUCTS).")
    val targetProductIds: Set<Long>? = null,

    @Schema(description = "Set of Category IDs this rule applies to (used if applicability is CATEGORIES).")
    val targetCategoryIds: Set<Long>? = null,

    @Schema(description = "Set of Brand IDs this rule applies to (used if applicability is BRANDS).")
    val targetBrandIds: Set<Long>? = null,

    @field:Positive(message = "Minimum quantity must be positive if specified.")
    @Schema(
        description = "Optional minimum quantity of applicable items required to trigger the discount.",
        example = "2"
    )
    val minimumQuantity: Int? = null,

    @field:DecimalMin(value = "0.01", message = "Minimum spend must be positive if specified.")
    @Schema(description = "Optional minimum spend amount required to trigger the discount.", example = "50.00")
    val minimumSpendAmount: BigDecimal? = null,

    @field:Size(min = 3, max = 3, message = "Minimum spend currency must be a 3-letter code.")
    @Schema(description = "Currency code for the minimum spend amount (required if amount is set).", example = "USD")
    val minimumSpendCurrency: String? = null,

    @field:NotNull(message = "Combinable status must be provided.")
    @Schema(
        description = "Can this discount be combined with others?",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isCombinable: Boolean = false
) {

    init {
        if (minimumSpendAmount != null) {
            require(!minimumSpendCurrency.isNullOrBlank()) { "Minimum spend currency must be provided if amount is set." }
        }
        // TODO: Add validation: target IDs should only be non-empty if applicability matches
    }
}