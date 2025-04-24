package dev.cnpe.ventescabekotlin.business.dto

import java.math.BigDecimal

/**
 * DTO carrying essential payment-related configuration for a business.
 */
data class BusinessPaymentData(
    val currencyCode: String,
    val taxPercentage: BigDecimal,
    val currencyScale: Int
)
