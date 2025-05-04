package dev.cnpe.ventescaposbe.customers.application.api

import dev.cnpe.ventescaposbe.customers.application.api.dto.CustomerBasicInfo

/**
 * Port defining operations for retrieving basic Customer information needed by other modules.
 */
interface CustomerInfoPort {

    /**
     * Retrieves basic information for a specific customer.
     *
     * @param customerId The ID of the customer.
     * @return CustomerBasicInfo if found, null otherwise.
     */
    fun getCustomerBasicInfo(customerId: Long): CustomerBasicInfo?

    /**
     * Checks if a customer with the given ID exists and is active.
     *
     * @param customerId The ID of the customer.
     * @return True if the customer exists and is active, false otherwise.
     */
    fun doesCustomerExist(customerId: Long): Boolean
}