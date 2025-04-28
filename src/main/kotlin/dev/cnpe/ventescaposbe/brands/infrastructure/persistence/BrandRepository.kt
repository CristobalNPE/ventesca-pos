package dev.cnpe.ventescaposbe.brands.infrastructure.persistence

import dev.cnpe.ventescaposbe.brands.domain.model.Brand
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BrandRepository : JpaRepository<Brand, Long> {

    fun existsByName(name: String): Boolean

    @Query(
        """
        select b.code.codeValue
        from Brand b
        where b.id = :brandId
        """
    )
    fun getBrandCodeById(brandId: Long): String?

    fun getBrandByIsDefaultTrue(): Brand?
}