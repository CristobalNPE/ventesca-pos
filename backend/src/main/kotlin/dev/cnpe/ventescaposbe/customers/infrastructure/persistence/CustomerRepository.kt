package dev.cnpe.ventescaposbe.customers.infrastructure.persistence

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.customers.domain.model.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime

interface CustomerRepository : JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    fun existsByEmail(email: String): Boolean

    fun existsByEmailAndIdNot(email: String, id: Long): Boolean

    @Query(
        """
        select (count(c) > 0) 
        from Customer c 
        where c.personalInfo.personalId = ?1
        """
    )
    fun existsByPersonalId(taxId: String): Boolean

    @Query(
        """
            select (count(c) > 0) 
            from Customer c 
            where c.personalInfo.personalId = ?1 and c.id <> ?2
            """
    )
    fun existsByPersonalIdExcludingSelf(taxId: String, id: Long): Boolean
}