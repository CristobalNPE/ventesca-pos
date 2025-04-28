package dev.cnpe.ventescaposbe.inventory.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reason for modifying the stock quantity.")
enum class StockModificationReason : DomainEnum {

    @Schema(description = "Initial stock quantity for a newly registered product.")
    INITIAL,

    @Schema(description = "Stock reduction due to product sale.")
    SALE,

    @Schema(description = "Stock increase due to customer product return.")
    RETURN,

    @Schema(description = "Stock reduction due to returning products to the supplier.")
    VENDOR_RETURN,

    @Schema(description = "Stock reduction due to lost products.")
    LOST,

    @Schema(description = "Stock reduction due to damaged or defective products.")
    DAMAGED,

    @Schema(description = "Stock increase due to product replenishment.")
    RESTOCK,

    @Schema(description = "Manual stock adjustment due to inventory errors.")
    CORRECTION,

    @Schema(description = "Stock modification for an unspecified reason.")
    OTHER
}