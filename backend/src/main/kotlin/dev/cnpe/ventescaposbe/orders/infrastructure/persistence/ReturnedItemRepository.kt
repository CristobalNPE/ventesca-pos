package dev.cnpe.ventescaposbe.orders.infrastructure.persistence

import dev.cnpe.ventescaposbe.orders.domain.entity.ReturnedItem
import org.springframework.data.jpa.repository.JpaRepository

interface ReturnedItemRepository : JpaRepository<ReturnedItem, Long> {
}