package dev.cnpe.ventescaposbe.orders.infrastructure.persistence

import dev.cnpe.ventescaposbe.orders.domain.entity.ReturnTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ReturnTransactionRepository: JpaRepository<ReturnTransaction,Long>, JpaSpecificationExecutor<ReturnTransaction> {
}