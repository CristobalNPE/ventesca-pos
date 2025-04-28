package dev.cnpe.ventescaposbe.suppliers.infrastructure.persistence

import dev.cnpe.ventescaposbe.suppliers.domain.Supplier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SupplierRepository : JpaRepository<Supplier, Long> {

    fun existsByName(name: String): Boolean

    @Query(
        """
            select count(s) > 0 
            from Supplier s
            where s.isDefault = true
        """
    )
    fun existsDefaultSupplier(): Boolean

    fun getSupplierByDefaultIsTrue(): Supplier?
}