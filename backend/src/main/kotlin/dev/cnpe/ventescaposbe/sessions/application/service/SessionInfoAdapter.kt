package dev.cnpe.ventescaposbe.sessions.application.service

import dev.cnpe.ventescaposbe.sessions.application.api.SessionInfoPort
import dev.cnpe.ventescaposbe.sessions.application.api.dto.SessionBasicInfo
import dev.cnpe.ventescaposbe.sessions.application.mapper.SessionMapper
import dev.cnpe.ventescaposbe.sessions.domain.enums.SessionStatus
import dev.cnpe.ventescaposbe.sessions.infrastructure.persistence.RegisterSessionRepository
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class SessionInfoAdapter(
    private val sessionRepository: RegisterSessionRepository,
    private val sessionMapper: SessionMapper
) : SessionInfoPort {

    override fun findOpenSession(userIdpId: String, branchId: Long): SessionBasicInfo? {
        log.debug { "Port: Finding open session for User: $userIdpId, Branch: $branchId" }

        val session = (sessionRepository.findByUserIdpIdAndBranchIdAndStatus(userIdpId, branchId, SessionStatus.OPEN)
            ?: throw createResourceNotFoundException("Open session for userId", userIdpId))

        return sessionMapper.toBasicInfo(session)
    }

    override fun isSessionOpen(userIdpId: String, branchId: Long): Boolean {
        log.debug { "Port: Checking if session is open for User: $userIdpId, Branch: $branchId" }
        val isOpen = sessionRepository.existsByUserIdpIdAndBranchIdAndStatus(userIdpId, branchId, SessionStatus.OPEN)
        log.trace { "Port: Session open status for User: $userIdpId, Branch: $branchId is $isOpen" }
        return isOpen
    }
}