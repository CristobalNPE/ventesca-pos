package dev.cnpe.ventescaposbe.catalog.infrastructure.persistence

import dev.cnpe.ventescaposbe.catalog.domain.model.ProductPrice
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProductPriceRepository : JpaRepository<ProductPrice, Long> {


    @Query(
        value = """
            select pp from ProductPrice pp
            where pp.product.id = :productId
            order by pp.startDate desc
        """,
        countQuery = """
            select count(pp)
            from ProductPrice pp
            where pp.product.id = :productId
        """
    )
    fun getProductPriceHistory(productId: Long, pageable: Pageable): Page<ProductPrice>

}