package dev.cnpe.ventescaposbe.business.application.api

import dev.cnpe.ventescaposbe.business.application.dto.response.BusinessBranchInfo
import dev.cnpe.ventescaposbe.business.domain.enums.BusinessStatus
import dev.cnpe.ventescaposbe.business.dto.BusinessPaymentData

/**
 * Port defining operations to retrieve essential business data,
 * often needed by other modules or infrastructure components like filters.
 */
interface BusinessDataPort {

    /**
     * Finds the tenant identifier associated with a given user id from the IDP
     * Returns null if the user is not found or not associated with a tenant.
     * Typically, queries the master database.
     */
    fun getTenantIdForUser(idpUserId: String): String?

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

    /**
     * Retrieves the name of the current business associated with the active security or tenant context.
     * The tenant context must be properly set to get the business name.
     *
     * @return the name of the current business
     */
    fun getCurrentBusinessName(): String?


    /**
     * Retrieves the brand message of the current business associated with the
     * active security or tenant context.
     * The tenant context must be properly set to obtain the brand message.
     *
     * @return the brand message of the current business, or null if not available
     */
    fun getCurrentBusinessBrandMessage(): String?


    /**
     * Retrieves detailed information about a specific business branch based on the provided branch ID.
     *
     * @param branchId the unique identifier of the branch to fetch details for
     * @return the details of the business branch, encapsulated in a BusinessBranchInfo object
     */
    fun getBranchDetails(branchId:Long): BusinessBranchInfo //fixme: this is coupled with internal dto for now

}