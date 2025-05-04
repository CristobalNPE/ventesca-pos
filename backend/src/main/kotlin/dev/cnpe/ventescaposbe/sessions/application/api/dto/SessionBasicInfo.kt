package dev.cnpe.ventescaposbe.sessions.application.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Basic information about an open register session.")
data class SessionBasicInfo(

    @Schema(description = "Unique ID of the session.")
    val sessionId: Long,

    @Schema(description = "System-generated session number.")
    val sessionNumber: String,

    @Schema(description = "Timestamp when the session was opened.")
    val openingTime: OffsetDateTime
)