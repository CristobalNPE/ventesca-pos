package dev.cnpe.ventescaposbe.sessions.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Type of manual cash movement within a session.")
enum class CashMovementType : DomainEnum {

    @Schema(description = "Cash added to the register.")
    PAY_IN,

    @Schema(description = "Cash removed from the register.")
    PAY_OUT
}