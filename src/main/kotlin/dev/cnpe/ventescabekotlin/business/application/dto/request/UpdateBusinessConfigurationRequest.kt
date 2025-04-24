package dev.cnpe.ventescabekotlin.business.application.dto.request

import dev.cnpe.ventescabekotlin.business.domain.enums.PaymentMethod
import java.math.BigDecimal

//TODO: We need to add validation and Schemas to this dto
data class UpdateBusinessConfigurationRequest(
    val currencyCode: String?,
    val taxPercentage: BigDecimal?,
    val acceptedPaymentMethods: Set<PaymentMethod>?
)