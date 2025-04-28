package dev.cnpe.ventescaposbe.brands.domain.model

import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import dev.cnpe.ventescaposbe.shared.domain.vo.GeneratedCode
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "brands")
class Brand(

    @Column(name = "name", nullable = false)
    var name: String,

    @Embedded
    var code: GeneratedCode,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,

    id: Long? = null,
    version: Int = 0

) : BaseEntity(id, version) {

    fun updateCodeValue(newCodeValue: String) {
        this.code = GeneratedCode(newCodeValue)
    }
}