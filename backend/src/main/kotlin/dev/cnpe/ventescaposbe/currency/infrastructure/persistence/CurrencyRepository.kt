package dev.cnpe.ventescaposbe.currency.infrastructure.persistence

import dev.cnpe.ventescaposbe.currency.domain.model.Currency
import org.springframework.data.jpa.repository.JpaRepository

interface CurrencyRepository : JpaRepository<Currency, Long> {

    fun findByCodeAndIsActiveTrue(code: String): Currency?

    fun existsByCodeAndIsActiveTrue(code: String): Boolean
}