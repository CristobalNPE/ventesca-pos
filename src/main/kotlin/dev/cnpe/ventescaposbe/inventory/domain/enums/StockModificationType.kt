package dev.cnpe.ventescaposbe.inventory.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Type of stock modification (increase or decrease).")
enum class StockModificationType : DomainEnum {

    @Schema(description = "Increase in available stock quantity.")
    INCREASE,

    @Schema(description = "Decrease in available stock quantity.")
    DECREASE
}