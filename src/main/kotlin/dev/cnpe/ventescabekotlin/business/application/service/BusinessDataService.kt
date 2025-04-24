package dev.cnpe.ventescabekotlin.business.application.service

import dev.cnpe.ventescabekotlin.business.application.api.BusinessDataPort
import dev.cnpe.ventescabekotlin.business.domain.enums.BusinessStatus
import dev.cnpe.ventescabekotlin.business.domain.model.BusinessUser
import dev.cnpe.ventescabekotlin.business.dto.BusinessPaymentData
import dev.cnpe.ventescabekotlin.business.infrastructure.persistence.BusinessUserRepository
import dev.cnpe.ventescabekotlin.currency.infrastructure.persistence.CurrencyRepository
import dev.cnpe.ventescabekotlin.security.context.UserContext
import dev.cnpe.ventescabekotlin.shared.application.exception.DomainException
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode.INSUFFICIENT_CONTEXT
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode.RESOURCE_NOT_FOUND
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
// This service mostly reads data. Some reads query master, others query tenant.
// Be careful with transaction boundaries if mixing master/tenant calls significantly.
// Default transaction manager is the tenant one. Use "masterTransactionManager" explicitly if needed.
@Transactional(readOnly = true)
open class BusinessDataService(
    private val businessUserRepository: BusinessUserRepository,
    private val currencyRepository: CurrencyRepository,
    private val userContext: UserContext
) : BusinessDataPort {

    @Transactional(readOnly = true, transactionManager = "masterTransactionManager")
    override fun getTenantIdForUser(idpUserId: String): String? {
        log.debug { "Querying master DB for tenant ID for user IdP ID: $idpUserId" }
        return businessUserRepository.findTenantIdByIdpUserId(idpUserId)
    }

    override fun getCurrentBusinessStatus(): BusinessStatus {
        val businessUser = getCurrentBusinessUserOrThrow()
        return businessUser.business?.statusInfo?.status
            ?: throw IllegalStateException("Business or StatusInfo is null for user ${businessUser.userEmail}")
    }

    override fun getBusinessMainBranchId(): Long {
        val businessUser = getCurrentBusinessUserOrThrow()
        return businessUser.business?.getMainBranch()?.id
            ?: throw DomainException(
                RESOURCE_NOT_FOUND,
                mapOf("entityType" to "Main BusinessBranch", "idpUserId" to businessUser.idpUserId)
            )
    }

    override fun getBusinessPaymentData(): BusinessPaymentData {
        val businessUser = getCurrentBusinessUserOrThrow()
        val business = businessUser.business
            ?: throw IllegalStateException("Business is null for user ${businessUser.userEmail}")

        val config = business.configuration
            ?: throw DomainException(
                RESOURCE_NOT_FOUND,
                mapOf("entityType" to "BusinessConfiguration", "businessId" to business.id!!)
            )

        // This queries the MASTER database within a TENANT transaction context.
        // Should be OK for read-only if masterDataSource is accessible.
        log.debug { "Querying master DB for currency info within tenant context..." }
        val currency = currencyRepository.findByCodeAndIsActiveTrue(config.currencyCode)
            ?: throw DomainException(
                RESOURCE_NOT_FOUND,
                mapOf("entityType" to "Active Currency", "code" to config.currencyCode)
            )

        return BusinessPaymentData(
            currencyCode = config.currencyCode,
            taxPercentage = config.taxPercentage,
            currencyScale = currency.scale
        )
    }

    override fun getBusinessBranchIds(): Set<Long> {
        val businessUser = getCurrentBusinessUserOrThrow()
        return businessUser.business?.branches
            ?.mapNotNull { it.id }
            ?.toSet()
            ?: emptySet()
    }

    // *******************************
    // 🔰 Private Helpers
    // *******************************

    /**
     * Retrieves the BusinessUser associated with the current request's authenticated user.
     * Uses the injected UserContext bean. Requires master transaction manager.
     * Throws DomainException if user IdP Id is missing or user is not found in master DB.
     */
    @Transactional(readOnly = true, transactionManager = "masterTransactionManager")
    protected fun getCurrentBusinessUserOrThrow(): BusinessUser {
        // Get IdP ID (sub claim) from the request-scoped context bean
        val currentUserIdpId = userContext.userId
            ?: throw DomainException(
                errorCode = INSUFFICIENT_CONTEXT,
                details = mapOf("missingContext" to "User IdP ID"),
                message = "Required IdP User ID not found in security context."
            )

        log.debug { "Querying master DB for BusinessUser by IdP ID: $currentUserIdpId" }
        return businessUserRepository.findByIdpUserId(currentUserIdpId)
            ?: throw DomainException(
                RESOURCE_NOT_FOUND,
                mapOf("entityType" to "BusinessUser", "idpUserId" to currentUserIdpId)
            )
    }

}