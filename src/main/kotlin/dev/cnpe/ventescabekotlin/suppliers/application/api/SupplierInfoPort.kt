package dev.cnpe.ventescabekotlin.suppliers.application.api

/**
 * Port defining operations for retrieving basic Supplier information needed by other modules.
 */
interface SupplierInfoPort {

    /**
     * Retrieves the unique identifier of the default supplier configured for the tenant.
     *
     * @return The ID of the default supplier.
     * @throws DomainException (e.g., RESOURCE_NOT_FOUND) if no default supplier is configured or found.
     */
    fun getDefaultSupplierId(): Long
}