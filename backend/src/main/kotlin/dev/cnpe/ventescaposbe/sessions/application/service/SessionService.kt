package dev.cnpe.ventescaposbe.sessions.application.service

import dev.cnpe.ventescaposbe.business.application.api.BusinessDataPort
import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.orders.application.api.OrderInfoPort
import dev.cnpe.ventescaposbe.orders.application.api.ReturnInfoPort
import dev.cnpe.ventescaposbe.security.context.UserContext
import dev.cnpe.ventescaposbe.sessions.application.api.SessionInfoPort
import dev.cnpe.ventescaposbe.sessions.application.dto.request.CloseSessionRequest
import dev.cnpe.ventescaposbe.sessions.application.dto.request.OpenSessionRequest
import dev.cnpe.ventescaposbe.sessions.application.dto.request.RecordCashMovementRequest
import dev.cnpe.ventescaposbe.sessions.application.dto.response.CashMovementResponse
import dev.cnpe.ventescaposbe.sessions.application.dto.response.RegisterSessionResponse
import dev.cnpe.ventescaposbe.sessions.application.mapper.SessionMapper
import dev.cnpe.ventescaposbe.sessions.domain.entity.CashMovement
import dev.cnpe.ventescaposbe.sessions.domain.entity.RegisterSession
import dev.cnpe.ventescaposbe.sessions.domain.enums.CashMovementType
import dev.cnpe.ventescaposbe.sessions.domain.enums.SessionStatus
import dev.cnpe.ventescaposbe.sessions.infrastructure.persistence.CashMovementRepository
import dev.cnpe.ventescaposbe.sessions.infrastructure.persistence.RegisterSessionRepository
import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode.OPERATION_NOT_ALLOWED
import dev.cnpe.ventescaposbe.shared.application.exception.createInvalidStateException
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

@Service
@Transactional
class SessionService(
    private val sessionRepository: RegisterSessionRepository,
    private val cashMovementRepository: CashMovementRepository,
    private val sessionMapper: SessionMapper,
    private val userContext: UserContext,
    private val businessDataPort: BusinessDataPort,
    private val moneyFactory: MoneyFactory,
    private val sessionInfoPort: SessionInfoPort,
    private val orderInfoPort: OrderInfoPort,
    private val returnInfoPort: ReturnInfoPort
) {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd")
        private const val SESSION_PREFIX = "SESS-"
    }

    fun openSession(request: OpenSessionRequest): RegisterSessionResponse {
        val userId = userContext.userId
            ?: throw DomainException(GeneralErrorCode.INSUFFICIENT_CONTEXT, message = "User ID missing.")
        val branchId =
            request.branchId  // TODO: Check this from the front. We would have to 'pick' the branch we want to use, or send the 'assigned branch'

        val allowedBranches = userContext.allowedBranchIds
            ?: throw DomainException(GeneralErrorCode.INSUFFICIENT_CONTEXT, message = "Allowed branches missing.")
        if (!allowedBranches.contains(branchId)) {
            throw DomainException(
                errorCode = OPERATION_NOT_ALLOWED,
                details = mapOf(
                    "reason" to "NOT_ALLOWED_IN_BRANCH",
                    "userId" to userId,
                    "requestedBranchId" to branchId
                )
            )
        }

        log.info { "Attempting to open session for User: $userId, Branch: $branchId" }

        if (sessionInfoPort.isSessionOpen(userId, branchId)) {
            throw createInvalidStateException(reason = "SESSION_ALREADY_OPEN")
        }

        val currencyCode = businessDataPort.getBusinessPaymentData().currencyCode
        val openingFloat = moneyFactory.createMoney(request.openingFloatAmount, currencyCode)
        val sessionNumber = generateSessionNumber()

        val newSession = RegisterSession(
            sessionNumber = sessionNumber,
            branchId = branchId,
            userIdpId = userId,
            openingFloat = openingFloat
        )

        val savedSession = sessionRepository.save(newSession)
        log.info { "Session opened successfully: ID=${savedSession.id}, Number=${savedSession.sessionNumber}" }
        return sessionMapper.toResponse(savedSession)
    }

    fun closeSession(sessionId: Long, request: CloseSessionRequest): RegisterSessionResponse {
        val userId = userContext.userId
            ?: throw DomainException(GeneralErrorCode.INSUFFICIENT_CONTEXT, message = "User ID missing.")

        log.info { "Attempting to close session ID: $sessionId by User: $userId" }

        val session = sessionRepository.findById(sessionId).orElseThrow {
            createResourceNotFoundException("RegisterSession", sessionId)
        }

        require(session.status == SessionStatus.OPEN) { "Session $sessionId is not OPEN." }
        require(session.userIdpId == userId) { "User $userId is not the owner of session $sessionId." }

        val currencyCode = session.openingFloat.currencyCode
        val countedCash = moneyFactory.createMoney(request.countedCashAmount, currencyCode)
        val zero = moneyFactory.zero(currencyCode)

        log.debug { "Calculating session totals for closing session ID: $sessionId" }
        val cashSales = orderInfoPort.calculateSessionCashSales(sessionId)
        val totalSales = orderInfoPort.calculateSessionTotalSales(sessionId)
        val cashRefunds = returnInfoPort.calculateSessionCashRefunds(sessionId)
        val totalRefunds = returnInfoPort.calculateSessionTotalRefunds(sessionId)

        val movements = cashMovementRepository.findBySessionId(sessionId)
        val payIns = movements.filter { it.type == CashMovementType.PAY_IN }.fold(zero) { sum, mov -> sum + mov.amount }
        val payOuts =
            movements.filter { it.type == CashMovementType.PAY_OUT }.fold(zero) { sum, mov -> sum + mov.amount }
        log.debug { "Calculated PayIns: $payIns, PayOuts: $payOuts for session $sessionId" }

        session.closeSession(
            countedCash = countedCash,
            cashSales = cashSales,
            cashRefunds = cashRefunds,
            payIns = payIns,
            payOuts = payOuts,
            totalSales = totalSales,
            totalRefunds = totalRefunds,
            closingNotes = request.notes
        )

        val savedSession = sessionRepository.save(session)
        log.info {
            "Session ID: $sessionId closed successfully. Expected: ${session.calculatedExpectedCash}, Counted: ${session.closingCountedCash}, Variance: ${session.cashVariance}"
        }

        // TODO: Publish SessionClosedEvent if needed for reporting or other integrations

        return sessionMapper.toResponse(savedSession)
    }

    fun recordCashMovement(sessionId: Long, request: RecordCashMovementRequest): CashMovementResponse {
        val userId = userContext.userId
            ?: throw DomainException(GeneralErrorCode.INSUFFICIENT_CONTEXT, message = "User ID missing.")

        log.info { "Recording cash movement for Session ID: $sessionId by User: $userId - Type: ${request.type}, Amount: ${request.amount}, Reason: ${request.reason}" }

        val session = sessionRepository.findById(sessionId).orElseThrow {
            createResourceNotFoundException("RegisterSession", sessionId)
        }

        require(session.status == SessionStatus.OPEN) { "Cash movements can only be recorded for OPEN sessions. Session $sessionId status is ${session.status}." }
        require(session.userIdpId == userId) { "User $userId cannot record cash movement for session $sessionId owned by ${session.userIdpId}." }

        val currencyCode = session.openingFloat.currencyCode
        val movementAmount = moneyFactory.createMoney(request.amount, currencyCode)

        val cashMovement = CashMovement(
            session = session,
            type = request.type,
            amount = movementAmount,
            reason = request.reason,
            notes = request.notes,
            userIdpId = userId
        )

        val savedMovement = cashMovementRepository.save(cashMovement)
        log.info { "Cash movement recorded successfully. ID: ${savedMovement.id}" }

        return sessionMapper.toCashMovementResponse(savedMovement)
    }

    @Transactional(readOnly = true)
    fun getCurrentSessionDetails(sessionId: Long): RegisterSessionResponse {
        log.debug { "Fetching details for session ID: $sessionId" }
        val session = sessionRepository.findById(sessionId).orElseThrow {
            createResourceNotFoundException("RegisterSession", sessionId)
        }
        // TODO: Add permission check - can the current user view this session?
        return sessionMapper.toResponse(session)
    }

    @Transactional(readOnly = true)
    fun findMyOpenSession(): RegisterSessionResponse? {
        val userId = userContext.userId
            ?: throw DomainException(GeneralErrorCode.INSUFFICIENT_CONTEXT, message = "User ID missing.")

        val allowedBranches = userContext.allowedBranchIds

        if (allowedBranches.isNullOrEmpty()) {
            log.warn { "User $userId has no allowed branches, cannot find open session." }
            return null
        }
        val openSession =
            sessionRepository.findFirstOpenSessionForUserInBranches(userId, allowedBranches)

        return openSession?.let {
            log.debug { "Found open session for user $userId: ID=${it.id}" }
            sessionMapper.toResponse(it)
        } ?: run {
            log.debug { "No open session found for user $userId in branches $allowedBranches" }
            null
        }
    }

    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun generateSessionNumber(): String {
        val datePart = LocalDate.now().format(DATE_FORMATTER)
        val sequenceNumber = try {
            sessionRepository.getNextSessionNumberSequenceValue()
        } catch (e: Exception) {
            log.error(e) { "Failed to retrieve next value from session_number_seq. Cannot generate session number reliably." }
            // TODO: FALLBACK

        }
        val number = "$SESSION_PREFIX$datePart-${sequenceNumber.toString().padStart(4, '0')}"
        log.debug { "Generated session number: $number" }
        return number
    }
}