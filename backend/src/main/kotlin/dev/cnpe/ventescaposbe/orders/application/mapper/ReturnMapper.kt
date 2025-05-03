package dev.cnpe.ventescaposbe.orders.application.mapper

import dev.cnpe.ventescaposbe.orders.application.dto.response.ReturnTransactionResponse
import dev.cnpe.ventescaposbe.orders.application.dto.response.ReturnedItemResponse
import dev.cnpe.ventescaposbe.orders.domain.entity.ReturnTransaction
import dev.cnpe.ventescaposbe.orders.domain.entity.ReturnedItem
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import org.springframework.stereotype.Component

@Component
class ReturnMapper {

    fun toResponse(entity: ReturnTransaction): ReturnTransactionResponse {
        return ReturnTransactionResponse(
            id = entity.id!!,
            originalOrderId = entity.originalOrderId,
            originalOrderNumber = entity.originalOrderNumber,
            status = entity.status,
            branchId = entity.branchId,
            userIdpId = entity.userIdpId,
            customerId = entity.customerId,
            returnTimestamp = entity.returnTimestamp,
            totalRefundAmount = entity.totalRefundAmount,
            refundMethod = entity.refundMethod,
            notes = entity.notes,
            returnedItems = entity.returnedItems.map { toItemResponse(it) },
            auditData = ResourceAuditData.fromBaseEntity(entity)
        )
    }

    fun toItemResponse(entity: ReturnedItem): ReturnedItemResponse {
        return ReturnedItemResponse(
            id = entity.id!!,
            originalOrderItemId = entity.originalOrderItemId,
            productId = entity.productId,
            productName = entity.productNameSnapshot,
            sku = entity.skuSnapshot,
            quantityReturned = entity.quantityReturned,
            unitRefundAmount = entity.unitRefundAmount,
            totalItemRefundAmount = entity.totalItemRefundAmount,
            reason = entity.reason,
            restock = entity.restock
        )
    }
}