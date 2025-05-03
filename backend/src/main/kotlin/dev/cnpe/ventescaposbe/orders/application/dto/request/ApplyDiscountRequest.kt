package dev.cnpe.ventescaposbe.orders.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "Request to apply a predefined discount rule.")
data class ApplyDiscountRequest(

    @Schema(
        description = "The ID of the DiscountRule (promotion/manual discount) to apply.",
        example = "105",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:NotNull(message = "Discount Rule ID must be provided.")
    @field:Positive(message = "Discount Rule ID must be positive.")
    val discountRuleId: Long
)