package dev.cnpe.ventescabekotlin.brands.domain.model

import dev.cnpe.ventescabekotlin.shared.domain.model.BaseEntity
import dev.cnpe.ventescabekotlin.shared.domain.vo.GeneratedCode
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "brands")
open class Brand(

    @Column(name = "name", nullable = false)
    open var name: String,

    @Embedded
    open var code: GeneratedCode,

    @Column(name = "is_default", nullable = false)
    open var isDefault: Boolean = false,

    id: Long? = null,
    version: Int = 0

) : BaseEntity(id, version) {

    fun updateCodeValue(newCodeValue: String) {
        this.code = GeneratedCode(newCodeValue)
    }
}