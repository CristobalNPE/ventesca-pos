package dev.cnpe.ventescabekotlin.business.application.api

import dev.cnpe.ventescabekotlin.business.domain.enums.BusinessStatus
import dev.cnpe.ventescabekotlin.business.dto.BusinessPaymentData

/**
 * Port defining operations to retrieve essential business data,
 * often needed by other modules or infrastructure components like filters.
 */
interface BusinessDataPort {

    /**
     * Finds the tenant identifier associated with a given user identifier (e.g., email).
     * Returns null if the user is not found or not associated with a tenant.
     * Typically queries the master database.
     */
    fun getTenantIdForUser(userEmail: String): String?

    /**
     * Gets the current operational status of the business associated with the
     * current security/tenant context.
     * Requires tenant context to be set.
     */
    fun getCurrentBusinessStatus(): BusinessStatus

    /**
     * Gets the ID of the main branch for the business associated with the
     * current security/tenant context.
     * Requires tenant context to be set.
     */
    fun getBusinessMainBranchId(): Long

    /**
     * Gets payment-related data (currency, tax) for the business associated
     * with the current security/tenant context.
     * Requires tenant context to be set.
     */
    fun getBusinessPaymentData(): BusinessPaymentData

    /**
     * Gets the IDs of all branches for the business associated with the
     * current security/tenant context.
     * Requires tenant context to be set.
     */
    fun getBusinessBranchIds(): Set<Long>

}