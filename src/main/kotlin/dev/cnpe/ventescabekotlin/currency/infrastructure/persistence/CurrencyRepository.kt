package dev.cnpe.ventescabekotlin.currency.infrastructure.persistence

import dev.cnpe.ventescabekotlin.currency.domain.model.Currency
import org.springframework.data.jpa.repository.JpaRepository

interface CurrencyRepository : JpaRepository<Currency, Long> {

    fun findByCodeAndIsActiveTrue(code: String): Currency?

    fun existsByCodeAndIsActiveTrue(code: String): Boolean
}