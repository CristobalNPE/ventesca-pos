package dev.cnpe.ventescaposbe.business.infrastructure.persistence

import dev.cnpe.ventescaposbe.business.domain.model.BusinessBranch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BusinessBranchRepository : JpaRepository<BusinessBranch, Long> {

    @Query(
        """
            select bb.id
            from BusinessBranch bb
            where bb.business.id = :businessId
        """
    )
    fun findAllIdsByBusinessId(businessId: Long): Set<Long>

}