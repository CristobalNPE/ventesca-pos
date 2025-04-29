package dev.cnpe.ventescaposbe.categories.infrastructure.persistence

import dev.cnpe.ventescaposbe.categories.domain.model.Category
import dev.cnpe.ventescaposbe.shared.domain.vo.GeneratedCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CategoryRepository : JpaRepository<Category, Long> {


    @Query(
        """
        select c
        from Category c
        left join fetch c.subcategories
        where c.parent is null
        """
    )
    fun findAllParentCategories(): List<Category>

    @Query(
        """
        select c 
        from Category c
        where c.isDefault = true
        """
    )
    fun getDefaultCategory(): Category?

    @Query(
        """
        select c.code.codeValue
        from Category c
        where c.id = :categoryId
        """
    )
    fun getCategoryCodeById(categoryId: Long): String?

    fun existsByCode(code: GeneratedCode): Boolean

    fun existsByName(name: String): Boolean
}