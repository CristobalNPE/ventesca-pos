package dev.cnpe.ventescaposbe.categories.domain.model

import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import dev.cnpe.ventescaposbe.shared.domain.vo.GeneratedCode
import jakarta.persistence.*

@Entity
@Table(name = "categories")
class Category(

    @Column(name = "name", nullable = false, unique = true)
    var name: String,

    @Column(name = "color", nullable = false, length = 7)
    var color: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,

    @Embedded
    var code: GeneratedCode,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null,

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var subcategories: MutableSet<Category> = mutableSetOf(),

    id: Long? = null,
    version: Int = 0
) : BaseEntity(id, version) {

    fun updateCode(newCodeValue: String) {
        this.code = GeneratedCode(newCodeValue)
    }

    fun addSubcategory(child: Category) {
        child.parent = this
        this.subcategories.add(child)
    }

    fun removeChild(child: Category) {
        child.parent = null
        this.subcategories.remove(child)
    }

    fun assignParent(parent: Category) {
        this.parent = parent
        parent.addSubcategory(this)
    }

    fun isRootCategory(): Boolean {
        return this.parent == null
    }
}