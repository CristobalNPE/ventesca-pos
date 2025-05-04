package dev.cnpe.ventescaposbe.sessions.infrastructure.persistence

import dev.cnpe.ventescaposbe.sessions.domain.entity.CashMovement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal

interface CashMovementRepository : JpaRepository<CashMovement, Long> {


    @Query(
        """
            select cm
            from CashMovement cm
            where cm.session.id = :sessionId
            order by cm.timestamp asc
        """
    )
    fun findBySessionId(sessionId: Long): List<CashMovement>


    @Query(
        """
             select sum(cm.amount.amount)
             from CashMovement cm
             where cm.session.id = :sessionId
             and cm.type = 'PAY_IN'
         """
    )
    fun sumPayInsForSession(sessionId: Long): BigDecimal?

}