package dev.cnpe.ventescaposbe.orders.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reason for returning an item.")
enum class ReturnReason : DomainEnum {

    @Schema(description = "Item is defective or malfunctioning.")
    DEFECTIVE,

    @Schema(description = "Received item was not what was ordered.")
    WRONG_ITEM,

    @Schema(description = "Customer changed their mind about the purchase.")
    CHANGED_MIND,

    @Schema(description = "Item does not fit (e.g., clothing).")
    SIZE_ISSUE,

    @Schema(description = "Item was damaged upon receipt or in store.")
    DAMAGED,

    @Schema(description = "Other unspecified reason.")
    OTHER
}