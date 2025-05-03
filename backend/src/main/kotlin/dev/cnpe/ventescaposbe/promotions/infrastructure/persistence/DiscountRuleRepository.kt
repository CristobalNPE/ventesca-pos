package dev.cnpe.ventescaposbe.promotions.infrastructure.persistence

import dev.cnpe.ventescaposbe.promotions.domain.model.DiscountRule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime

interface DiscountRuleRepository : JpaRepository<DiscountRule, Long>, JpaSpecificationExecutor<DiscountRule> {


    @Query(
        """
            select dr
            from DiscountRule dr
            where dr.isActive = true
            and (dr.startDate is null or dr.startDate <= :now)
            and (dr.endDate is null or dr.endDate > :now)
        """
    )
    fun findActiveAndValidRules(now: OffsetDateTime): List<DiscountRule>

    fun findByName(name: String): DiscountRule?


}