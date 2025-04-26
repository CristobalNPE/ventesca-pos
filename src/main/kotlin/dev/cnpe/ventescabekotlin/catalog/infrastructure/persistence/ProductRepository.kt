package dev.cnpe.ventescabekotlin.catalog.infrastructure.persistence

import dev.cnpe.ventescabekotlin.catalog.domain.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    fun existsByBarcode(barcode: String): Boolean

    fun existsByName(name: String): Boolean

    fun countAllByCategoryId(categoryId: Long): Long

    fun countAllBySupplierId(supplierId: Long): Long

    fun countAllByBrandId(brandId: Long): Long

    fun findByBarcode(barcode: String): Product?

}