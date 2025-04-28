package dev.cnpe.ventescaposbe.currency.service

import dev.cnpe.ventescaposbe.currency.domain.model.Currency
import dev.cnpe.ventescaposbe.currency.exception.CurrencyNotFoundException
import dev.cnpe.ventescaposbe.currency.infrastructure.persistence.CurrencyRepository
import dev.cnpe.ventescaposbe.currency.vo.Money
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*


/**
 * Factory service for creating and formatting Money value objects.
 * Ensures created Money instances use supported and active currency codes
 * and applies correct scaling based on currency definition.
 */
@Component
class MoneyFactory(
    private val currencyRepository: CurrencyRepository
) {

    /**
     * Creates a Money instance for the given amount and currency code.
     * Validates that the currency code is supported and active.
     * Applies the correct scale (number of decimal places) for the currency.
     *
     * @param amount The monetary amount.
     * @param currencyCode The 3-letter ISO 4217 currency code.
     * @return A validated Money instance.
     * @throws CurrencyNotFoundException if the currency code is not supported or inactive.
     */
    fun createMoney(amount: BigDecimal, currencyCode: String): Money {
        val currency = findActiveCurrencyOrThrow(currencyCode)

        val scaledAmount = amount.setScale(currency.scale, RoundingMode.HALF_EVEN)
        return Money(scaledAmount, currency.code)
    }

    /**
     * Creates a Money instance with zero amount for the given currency code.
     * Validates the currency code.
     *
     * @param currencyCode The 3-letter ISO 4217 currency code.
     * @return A Money instance representing zero value in the specified currency.
     * @throws CurrencyNotFoundException if the currency code is not supported or inactive.
     */
    fun zero(currencyCode: String): Money {
        val currency = findActiveCurrencyOrThrow(currencyCode)
        val zeroAmount = BigDecimal.ZERO.setScale(currency.scale, RoundingMode.HALF_UP)
        return Money(zeroAmount, currency.code)
    }

    /**
     * Formats a Money object into a human-readable string using locale-specific conventions
     * and the currency's symbol and scale.
     *
     * @param money The Money object to format.
     * @param locale The locale to use for formatting (defaults to current request locale).
     * @return A formatted string representation (e.g., "$ 1,234.50", "¥1,000").
     * @throws CurrencyNotFoundException if the money's currency code is invalid (should not happen if created via factory).
     */
    fun format(money: Money, locale: Locale = LocaleContextHolder.getLocale()): String {
        val currency = findActiveCurrencyOrThrow(money.currencyCode)

        // NumberFormat for locale-aware currency formatting
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)

        val currencyInstance = java.util.Currency.getInstance(money.currencyCode)
        currencyFormat.currency = currencyInstance

        currencyFormat.minimumFractionDigits = currency.scale
        currencyFormat.maximumFractionDigits = currency.scale

        return currencyFormat.format(money.amount)
    }

    /**
     * Finds an active currency by code or throws CurrencyNotFoundException
     * */
    private fun findActiveCurrencyOrThrow(currencyCode: String): Currency {
        require(currencyCode.length == 3 && currencyCode.all { it.isUpperCase() }) {
            "Invalid currency code format: '$currencyCode'. Must be 3 uppercase letters."
        }
        return currencyRepository.findByCodeAndIsActiveTrue(currencyCode)
            ?: throw CurrencyNotFoundException(currencyCode)
    }

}