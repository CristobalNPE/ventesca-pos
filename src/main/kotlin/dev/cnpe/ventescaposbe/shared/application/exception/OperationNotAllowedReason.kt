package dev.cnpe.ventescaposbe.shared.application.exception

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Marker interface for specific reasons why an operation might not be allowed.")
interface OperationNotAllowedReason {
    val name: String
}