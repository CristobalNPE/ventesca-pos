package dev.cnpe.ventescaposbe.suppliers.application.api

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


    /**
     * Retrieves the name of the supplier based on the given supplier ID.
     *
     * @param id The unique identifier of the supplier whose name is to be retrieved.
     * @return The name of the supplier corresponding to the provided ID.
     * @throws DomainException (e.g., RESOURCE_NOT_FOUND) if no supplier is found with the given ID.
     */
    fun getSupplierNameById(id: Long): String


}