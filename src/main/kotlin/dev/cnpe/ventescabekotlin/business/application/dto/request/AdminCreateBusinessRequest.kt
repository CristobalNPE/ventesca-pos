package dev.cnpe.ventescabekotlin.business.application.dto.request

import dev.cnpe.ventescabekotlin.business.domain.enums.PaymentMethod
import java.math.BigDecimal


//TODO: We need to add validation and Schemas to this dto
data class AdminCreateBusinessRequest(
    val adminUserId: String, // IdP ID ('sub') of the user who will be the Business Admin
    val adminUserEmail: String,
    val businessName: String,
    val brandMessage: String?,
    val currencyCode: String,
    val taxPercentage: BigDecimal,
    val acceptedPaymentMethods: Set<PaymentMethod>,
    val contactPhone: String?,
    val contactEmail: String?,
    val contactWebsite: String?,
    val mainBranchName: String? = null,
    val mainBranchStreet: String?,
    val mainBranchCity: String?,
    val mainBranchZipCode: String?,
    val mainBranchCountry: String?

)
