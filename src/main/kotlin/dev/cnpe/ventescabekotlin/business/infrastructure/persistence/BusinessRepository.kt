package dev.cnpe.ventescabekotlin.business.infrastructure.persistence

import dev.cnpe.ventescabekotlin.business.domain.model.Business
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BusinessRepository : JpaRepository<Business, Long> {

    fun existsByAdminId(adminId: String): Boolean

    @Query(
        """
        select b
        from BusinessUser bu
        join bu.business b
        where bu.userEmail = :userEmail
        """
    )
    fun findBusinessByUserEmail(userEmail: String): Business?

    @Query(
        """
        select distinct b.tenantId.value
        from Business b 
        where b.tenantId.value is not null
        """
    )
    fun findAllDistinctTenantIds(): Set<String>


    fun existsByDetails_BusinessName(businessName: String): Boolean

}