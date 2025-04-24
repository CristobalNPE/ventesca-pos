package dev.cnpe.ventescabekotlin.business.infrastructure.persistence

import dev.cnpe.ventescabekotlin.business.domain.model.BusinessUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BusinessUserRepository : JpaRepository<BusinessUser, Long> {

    @Query(
        """
        select b.tenantId.value 
        from BusinessUser bu 
        join bu.business b 
        where bu.idpUserId = :idpUserId 
        """
    )
    fun findTenantIdByIdpUserId(idpUserId: String): String?

    fun findByIdpUserId(idpUserId: String): BusinessUser?

    fun findByUserEmail(userEmail: String): BusinessUser?
}