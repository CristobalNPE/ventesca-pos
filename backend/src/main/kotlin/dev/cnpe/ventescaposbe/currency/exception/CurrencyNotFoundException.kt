package dev.cnpe.ventescaposbe.currency.exception

/**
 * Exception thrown when an operation requires a currency that is not
 * supported, not active, or cannot be found in the system.
 */
class CurrencyNotFoundException(
    val currencyCode: String
) : RuntimeException("Currency not supported or not active: $currencyCode")