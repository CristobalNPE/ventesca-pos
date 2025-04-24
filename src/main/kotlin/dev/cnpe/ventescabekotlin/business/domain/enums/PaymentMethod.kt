package dev.cnpe.ventescabekotlin.business.domain.enums

import dev.cnpe.ventescabekotlin.shared.domain.enums.DomainEnum

/**
 * Represents the method through which a payment can be processed.
 * User-facing names and descriptions can be obtained via MessageSource
 * using keys formatted as "enum.PaymentMethod.<METHOD_NAME>.name" and "enum.PaymentMethod.<METHOD_NAME>.description".
 */
enum class PaymentMethod : DomainEnum {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    MOBILE_PAYMENT,
    GIFT_CARD,
    BANK_TRANSFER,
    CHECK,
    STORE_CREDIT,
    SPLIT_PAYMENT;
}