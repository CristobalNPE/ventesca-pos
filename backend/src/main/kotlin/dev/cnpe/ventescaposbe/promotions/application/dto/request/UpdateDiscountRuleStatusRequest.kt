package dev.cnpe.ventescaposbe.promotions.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull


@Schema(description = "Request payload for updating the active status of a discount rule.")
data class UpdateDiscountRuleStatusRequest(

    @field:NotNull(message = "Active status must be provided.")
    @Schema(
        description = "Set to true to activate the rule, false to deactivate.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isActive: Boolean
)
