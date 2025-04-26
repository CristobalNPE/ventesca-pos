package dev.cnpe.ventescabekotlin.catalog.domain.enums

import dev.cnpe.ventescabekotlin.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The status of the product within the catalog and sales channel.")
enum class ProductStatus : DomainEnum {

    @Schema(description = "Initial state, minimal info provided (name, barcode). Not sellable.")
    DRAFT,

    @Schema(description = "Actively listed, sellable, and visible (if stock allows).")
    ACTIVE,

    @Schema(description = "Temporarily delisted or disabled, not sellable.")
    INACTIVE;

}