package dev.cnpe.ventescabekotlin.inventory.domain.entity

import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockModificationReason
import dev.cnpe.ventescabekotlin.inventory.domain.enums.StockModificationType
import dev.cnpe.ventescabekotlin.shared.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table

@Entity
@Table(name = "stock_modifications")
class StockModification(


    @Column(name = "amount", nullable = false)
    var amount: Double,

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    var type: StockModificationType,

    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    var reason: StockModificationReason,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    var item: InventoryItem? = null,

    id: Long? = null,
    version: Int = 0

) : BaseEntity(id, version) {

    @PrePersist
    @PreUpdate
    private fun validateItemAssociation() {
        requireNotNull(item) { "StockModification must be associated with an InventoryItem" }
    }
}