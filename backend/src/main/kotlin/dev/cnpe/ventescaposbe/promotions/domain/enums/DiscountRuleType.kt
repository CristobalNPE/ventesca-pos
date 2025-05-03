package dev.cnpe.ventescaposbe.promotions.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The type of discount or promotion rule.")
enum class DiscountRuleType : DomainEnum {

    @Schema(description = "Percentage discount applied to specific item(s).")
    ITEM_PERCENTAGE,

    @Schema(description = "Fixed amount discount applied to specific item(s).")
    ITEM_FIXED_AMOUNT,

    @Schema(description = "Percentage discount applied to the order total (after item discounts).")
    ORDER_PERCENTAGE,

    @Schema(description = "Fixed amount discount applied to the order total (after item discounts).")
    ORDER_FIXED_AMOUNT

}