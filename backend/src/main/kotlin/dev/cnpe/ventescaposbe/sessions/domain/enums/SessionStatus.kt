package dev.cnpe.ventescaposbe.sessions.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The status of a cashier register session.")
enum class SessionStatus : DomainEnum {

    @Schema(description = "Session is currently open and active.")
    OPEN,

    @Schema(description = "Session has been closed and reconciled.")
    CLOSED
}