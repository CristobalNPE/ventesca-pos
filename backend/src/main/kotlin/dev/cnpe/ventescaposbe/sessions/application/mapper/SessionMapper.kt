package dev.cnpe.ventescaposbe.sessions.application.mapper

import dev.cnpe.ventescaposbe.sessions.application.api.dto.SessionBasicInfo
import dev.cnpe.ventescaposbe.sessions.application.dto.response.CashMovementResponse
import dev.cnpe.ventescaposbe.sessions.application.dto.response.RegisterSessionResponse
import dev.cnpe.ventescaposbe.sessions.domain.entity.CashMovement
import dev.cnpe.ventescaposbe.sessions.domain.entity.RegisterSession
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import org.springframework.stereotype.Component

@Component
class SessionMapper {

    fun toResponse(entity: RegisterSession): RegisterSessionResponse {
        return RegisterSessionResponse(
            id = entity.id!!,
            sessionNumber = entity.sessionNumber,
            branchId = entity.branchId,
            userIdpId = entity.userIdpId,
            registerId = entity.registerId,
            status = entity.status,
            openingTime = entity.openingTime,
            closingTime = entity.closingTime,
            openingFloat = entity.openingFloat,
            closingCountedCash = entity.closingCountedCash,
            calculatedCashSales = entity.calculatedCashSales,
            calculatedCashRefunds = entity.calculatedCashRefunds,
            calculatedPayIns = entity.calculatedPayIns,
            calculatedPayOuts = entity.calculatedPayOuts,
            calculatedExpectedCash = entity.calculatedExpectedCash,
            cashVariance = entity.cashVariance,
            totalSalesAmount = entity.totalSalesAmount,
            totalRefundAmount = entity.totalRefundAmount,
            notes = entity.notes,
            auditData = ResourceAuditData.fromBaseEntity(entity)
        )
    }

    fun toCashMovementResponse(entity: CashMovement): CashMovementResponse {
        requireNotNull(entity.session.id) { "Session ID cannot be null for CashMovement mapping" }
        return CashMovementResponse(
            id = entity.id!!,
            sessionId = entity.session.id!!,
            timestamp = entity.timestamp,
            type = entity.type,
            amount = entity.amount,
            reason = entity.reason,
            notes = entity.notes,
            userIdpId = entity.userIdpId,
            auditData = ResourceAuditData.fromBaseEntity(entity)
        )
    }

    fun toBasicInfo(entity: RegisterSession): SessionBasicInfo {
        return SessionBasicInfo(
            sessionId = entity.id!!,
            sessionNumber = entity.sessionNumber,
            openingTime = entity.openingTime
        )
    }
}