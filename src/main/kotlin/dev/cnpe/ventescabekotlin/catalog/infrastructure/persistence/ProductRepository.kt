package dev.cnpe.ventescabekotlin.catalog.infrastructure.persistence

import dev.cnpe.ventescabekotlin.catalog.domain.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    fun existsByBarcode(barcode: String): Boolean

    fun existsByName(name: String): Boolean

    fun countAllByCategoryId(categoryId: Long): Long

    fun countAllBySupplierId(supplierId: Long): Long

    fun countAllByBrandId(brandId: Long): Long

    fun findByBarcode(barcode: String): Product?

    @Query(
        value = """
            select distinct p
            from Product p
            left join fetch p.priceHistory ph
            order by p.createdAt desc
        """,
        countQuery = """
            select count(p)
            from Product p
        """
    )
    fun findAllWithPriceHistory(pageable: Pageable): Page<Product>

}