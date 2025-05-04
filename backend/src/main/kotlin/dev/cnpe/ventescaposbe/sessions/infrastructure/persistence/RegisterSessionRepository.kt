package dev.cnpe.ventescaposbe.sessions.infrastructure.persistence

import dev.cnpe.ventescaposbe.sessions.domain.entity.RegisterSession
import dev.cnpe.ventescaposbe.sessions.domain.enums.SessionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface RegisterSessionRepository : JpaRepository<RegisterSession, Long>, JpaSpecificationExecutor<RegisterSession> {

    fun findByUserIdpIdAndBranchIdAndStatus(
        userIdpId: String,
        branchId: Long,
        status: SessionStatus
    ): RegisterSession?

    fun existsByUserIdpIdAndBranchIdAndStatus(
        userIdpId: String,
        branchId: Long,
        status: SessionStatus
    ): Boolean


    @Query(
        """
            select rs
            from RegisterSession rs
            where rs.userIdpId = :userIdpId
            and rs.branchId in :branchIds
            and rs.status = :status
            order by rs.openingTime desc
            limit 1
        """
    )
    fun findFirstOpenSessionForUserInBranches(
        userIdpId: String,
        branchIds: Collection<Long>,
        status: SessionStatus = SessionStatus.OPEN
    ): RegisterSession?


    @Query(value = "SELECT NEXTVAL('session_number_seq')", nativeQuery = true)
    fun getNextSessionNumberSequenceValue(): Long
}