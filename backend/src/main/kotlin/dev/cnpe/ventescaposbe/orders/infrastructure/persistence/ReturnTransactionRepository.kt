package dev.cnpe.ventescaposbe.orders.infrastructure.persistence

import dev.cnpe.ventescaposbe.orders.domain.entity.ReturnTransaction
import dev.cnpe.ventescaposbe.orders.domain.enums.RefundMethod
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface ReturnTransactionRepository: JpaRepository<ReturnTransaction,Long>, JpaSpecificationExecutor<ReturnTransaction> {

    @Query(
        """
            select sum(rt.totalRefundAmount.amount)
            from ReturnTransaction rt
            where rt.sessionId = :sessionId
            and rt.refundMethod = :refundMethod
            and rt.status = 'COMPLETED'
        """
    )
    fun sumTotalRefundAmountBySessionIdAndMethod(
        @Param("sessionId") sessionId: Long,
        @Param("refundMethod") refundMethod: RefundMethod
    ): BigDecimal?


    @Query(
        """
            select sum(rt.totalRefundAmount.amount)
            from ReturnTransaction rt
            where rt.sessionId = :sessionId
            and rt.status = 'COMPLETED'
        """
    )
    fun sumTotalRefundAmountBySessionId(@Param("sessionId") sessionId: Long): BigDecimal?

}