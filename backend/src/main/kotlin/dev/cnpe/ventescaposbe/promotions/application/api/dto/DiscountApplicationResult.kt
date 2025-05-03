package dev.cnpe.ventescaposbe.promotions.application.api.dto

import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountRuleType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "Represents the details of a discount that should be applied based on promotion rules.")
data class DiscountApplicationResult(

    @Schema(description = "ID of the DiscountRule that triggered this discount.")
    val discountRuleId: Long,

    @Schema(description = "A descriptive name for the applied discount (e.g., '10% Off Summer Sale').")
    val description: String,

    @Schema(description = "The type of discount to apply (Percentage or Fixed Amount).")
    val type: DiscountRuleType,

    @Schema(description = "The value of the discount (e.g., 10.0 for 10%, or 5.00 for a fixed amount).")
    val value: BigDecimal,

    )