package dev.cnpe.ventescaposbe.currency.vo

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.math.BigDecimal

@Schema(description = "Represents a monetary value with an associated currency code.")
@Embeddable
data class Money(
    @Schema(description = "The monetary amount.", required = true, example = "199.99")
    @Column(name = "amount", nullable = false)
    val amount: BigDecimal,

    @Schema(description = "The 3-letter ISO 4217 currency code.", required = true, example = "USD")
    @Column(name = "currency_code", nullable = false, length = 3)
    val currencyCode: String
) : Comparable<Money> {

    fun isNegative(): Boolean = amount < BigDecimal.ZERO
    fun isPositive(): Boolean = amount > BigDecimal.ZERO
    fun isZero(): Boolean = amount.compareTo(BigDecimal.ZERO) == 0
    fun isNonNegative(): Boolean = amount >= BigDecimal.ZERO

    /** Adds another Money value (must have same currency). */
    operator fun plus(other: Money): Money {
        assertSameCurrency(other)
        return this.copy(amount = this.amount + other.amount)
    }

    /** Subtracts another Money value (must have same currency). */
    operator fun minus(other: Money): Money {
        assertSameCurrency(other)
        return this.copy(amount = this.amount - other.amount)
    }

    /** Multiplies the amount by a BigDecimal multiplier. */
    operator fun times(multiplier: BigDecimal): Money {
        return this.copy(amount = this.amount.multiply(multiplier))
    }

    /** Multiplies the amount by an Int multiplier. */
    operator fun times(multiplier: Int): Money {
        return this * multiplier.toBigDecimal()
    }

    /** Multiplies the amount by a Double multiplier (use with caution due to potential precision issues). */
    operator fun times(multiplier: Double): Money {
        return this * BigDecimal.valueOf(multiplier)
    }

    /** Negates the monetary amount. */
    operator fun unaryMinus(): Money {
        return this.copy(amount = this.amount.negate())
    }

    /** Compares this Money object with another based on amount (must have same currency). */
    override fun compareTo(other: Money): Int {
        assertSameCurrency(other)
        return this.amount.compareTo(other.amount)
    }

    /** Ensures the other Money object has the same currency code. Throws IllegalArgumentException if not. */
    fun assertSameCurrency(other: Money) {
        require(this.currencyCode == other.currencyCode) {
            "Currency mismatch: Cannot operate on Money values with different currencies (${this.currencyCode} vs ${other.currencyCode})"
        }
    }
}