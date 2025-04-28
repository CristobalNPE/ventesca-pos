package dev.cnpe.ventescaposbe.currency.domain.model

import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "supported_currencies")
class Currency(

    @Column(name = "code", nullable = false, unique = true, length = 3)
    var code: String,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "symbol", nullable = false)
    var symbol: String,

    @Column(name = "scale", nullable = false)
    var scale: Int,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean,

    id: Long? = null,
    version: Int = 0

) : BaseEntity(id, version)