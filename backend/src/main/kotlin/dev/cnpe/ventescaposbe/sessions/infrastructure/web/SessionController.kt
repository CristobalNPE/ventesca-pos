package dev.cnpe.ventescaposbe.sessions.infrastructure.web

import dev.cnpe.ventescaposbe.security.annotation.RequirePosOperatorRoles
import dev.cnpe.ventescaposbe.sessions.application.dto.request.CloseSessionRequest
import dev.cnpe.ventescaposbe.sessions.application.dto.request.OpenSessionRequest
import dev.cnpe.ventescaposbe.sessions.application.dto.request.RecordCashMovementRequest
import dev.cnpe.ventescaposbe.sessions.application.dto.response.CashMovementResponse
import dev.cnpe.ventescaposbe.sessions.application.dto.response.RegisterSessionResponse
import dev.cnpe.ventescaposbe.sessions.application.service.SessionService
import dev.cnpe.ventescaposbe.shared.application.dto.ApiResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/sessions")
@Tag(name = "Register Sessions", description = "Manage cashier register sessions (shifts).")
@RequirePosOperatorRoles
class SessionController(
    private val sessionService: SessionService
) {

    @PostMapping("/open")
    @Operation(summary = "Open a new register session")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Session opened successfully",
                content = [Content(schema = Schema(implementation = RegisterSessionResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data (e.g., negative float)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Forbidden (User not allowed for branch)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Conflict (Session already open for user/branch)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun openSession(@Valid @RequestBody request: OpenSessionRequest): ResponseEntity<RegisterSessionResponse> {
        val openedSession = sessionService.openSession(request)
        val location = URI.create("/sessions/${openedSession.id}")
        return ResponseEntity.created(location).body(openedSession)
    }

    @PostMapping("/{sessionId}/close")
    @Operation(summary = "Close an open register session")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Session closed successfully, includes calculated totals",
                content = [Content(schema = Schema(implementation = RegisterSessionResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data (e.g., negative counted cash)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Forbidden (User does not own this session)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Session not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Conflict (Session is not OPEN)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized")
        ]
    )
    fun closeSession(
        @Parameter(description = "ID of the session to close") @PathVariable sessionId: Long,
        @Valid @RequestBody request: CloseSessionRequest
    ): RegisterSessionResponse {
        return sessionService.closeSession(sessionId, request)
    }

    @PostMapping("/{sessionId}/cash-movements")
    @Operation(summary = "Record a manual cash movement (Pay-In/Pay-Out)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "Cash movement recorded successfully",
                content = [Content(schema = Schema(implementation = CashMovementResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid input data",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Session not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Conflict (Session is not OPEN)",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden") // If permission check added in service
        ]
    )
    fun recordCashMovement(
        @Parameter(description = "ID of the open session") @PathVariable sessionId: Long,
        @Valid @RequestBody request: RecordCashMovementRequest
    ): ResponseEntity<CashMovementResponse> {
        val movement = sessionService.recordCashMovement(sessionId, request)
        // TODO: maybe GET /sessions/{sid}/cash-movements/{mid} later for this locaiton to make sense?
        val location = URI.create("/sessions/$sessionId/cash-movements/${movement.id}")
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(movement)
    }

    @GetMapping("/my-open")
    @Operation(summary = "Get the current user's open session, if any")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Open session details retrieved (or empty body if none)",
                content = [Content(schema = Schema(implementation = RegisterSessionResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
        ]
    )
    fun getMyOpenSession(): ResponseEntity<RegisterSessionResponse> {
        val openSession = sessionService.findMyOpenSession()
        return if (openSession != null) {
            ResponseEntity.ok(openSession)
        } else {
            ResponseEntity.ok().build()
        }
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get details of a specific session (open or closed)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Session details retrieved",
                content = [Content(schema = Schema(implementation = RegisterSessionResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Session not found",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden")
        ]
    )
    fun getSessionDetails(
        @Parameter(description = "ID of the session") @PathVariable sessionId: Long
    ): RegisterSessionResponse {
        return sessionService.getCurrentSessionDetails(sessionId)
    }

    // TODO: Add endpoint later for GET /sessions/{sessionId}/cash-movements if needed
}