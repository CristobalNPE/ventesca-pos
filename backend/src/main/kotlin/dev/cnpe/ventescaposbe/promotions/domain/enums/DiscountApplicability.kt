package dev.cnpe.ventescaposbe.promotions.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Specifies what the discount rule applies to.")

enum class DiscountApplicability : DomainEnum {
    @Schema(description = "Applies to the entire order.")
    ORDER_TOTAL,

    @Schema(description = "Applies to specific product IDs.")
    SPECIFIC_PRODUCTS,

    @Schema(description = "Applies to products within specific category IDs.")
    CATEGORIES,

    @Schema(description = "Applies to products within specific brand IDs.")
    BRANDS

    //TODO: ALL_PRODUCTS, CUSTOMER_GROUP
}