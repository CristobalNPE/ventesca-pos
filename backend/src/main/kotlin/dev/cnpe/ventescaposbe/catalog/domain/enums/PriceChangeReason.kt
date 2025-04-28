package dev.cnpe.ventescaposbe.catalog.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The reason for a change in product price (selling or cost).")
enum class PriceChangeReason : DomainEnum {
    @Schema(description = "Price set when product is first created.")
    INITIAL,

    @Schema(description = "Price changed due to update in supplier cost.")
    COST_CHANGE,

    @Schema(description = "Temporary price adjustment for a promotion.")
    PROMOTION,

    @Schema(description = "Price change based on seasonality.")
    SEASONAL,

    @Schema(description = "Price adjusted due to competitor pricing.")
    MARKET_COMPETITION,

    @Schema(description = "Price change resulting from switching suppliers.")
    SUPPLIER_CHANGE,

    @Schema(description = "Manual price adjustment or correction.")
    MANUAL_ADJUSTMENT,

    @Schema(description = "Price change due to other unspecified reasons.")
    OTHER;
}