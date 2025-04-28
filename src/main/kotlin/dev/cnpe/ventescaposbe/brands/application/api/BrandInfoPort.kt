package dev.cnpe.ventescaposbe.brands.application.api

/**
 * Port defining operations for retrieving basic Brand information needed by other modules.
 */
interface BrandInfoPort {

    /**
     * Retrieves the unique code of a brand by its ID.
     *
     * @param brandId The ID of the brand.
     * @return The brand code string.
     * @throws DomainException (e.g., RESOURCE_NOT_FOUND) if the brand ID is invalid.
     */
    fun getBrandCodeById(brandId: Long): String

    /**
     * Retrieves the unique identifier of the default brand configured for the tenant.
     *
     * @return The ID of the default brand.
     * @throws DomainException (e.g., RESOURCE_NOT_FOUND) if no default brand is configured or found.
     */
    fun getDefaultBrandId(): Long
}