package dev.cnpe.ventescaposbe.catalog.application.util

import dev.cnpe.ventescaposbe.business.dto.BusinessPaymentData
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utility service providing helper functions related to product pricing and taxes.
 */
@Component
class ProductUtils {

    companion object {
        private val ONE_HUNDRED = BigDecimal(100)
        private val ONE = BigDecimal.ONE
    }

    /**
     * Adjusts a price based on whether it includes tax or needs tax added.
     *
     * @param price The base price amount.
     * @param paymentData Business data containing tax percentage and currency scale.
     * @param taxInclusive True if the input 'price' already includes tax (calculates net),
     *                     False if tax needs to be added to the 'price' (calculates gross).
     * @return The adjusted price (either net or gross).
     */
    fun applyTaxAdjustment(
        price: BigDecimal,
        paymentData: BusinessPaymentData,
        taxInclusive: Boolean
    ): BigDecimal {
        val taxRate = paymentData.taxPercentage.divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP)
        val taxFactor = ONE + taxRate
        val scale = paymentData.currencyScale
        val roundingMode = RoundingMode.HALF_UP

        return if (taxInclusive) {
            price.divide(taxFactor, scale, roundingMode)
        } else {
            price.multiply(taxFactor).setScale(scale, roundingMode)
        }
    }

    /**
     * Calculates the net price (excluding tax) from a given gross price (including tax).
     */
    fun calculateNetPrice(grossPrice: BigDecimal, paymentData: BusinessPaymentData): BigDecimal {
        return applyTaxAdjustment(grossPrice, paymentData, taxInclusive = true)
    }

    /**
     * Calculates the final gross price (including tax) from a given net price (excluding tax).
     */
    fun calculateFinalPrice(netPrice: BigDecimal, paymentData: BusinessPaymentData): BigDecimal {
        return applyTaxAdjustment(netPrice, paymentData, taxInclusive = false)
    }

}