package dev.cnpe.ventescabekotlin.categories.application.api

/**
 * Port defining operations for retrieving basic Category information needed by other modules.
 */
interface CategoryInfoPort {
    /**
     * Retrieves the unique code of a category by its ID.
     *
     * @param categoryId The ID of the category.
     * @return The category code string.
     * @throws DomainException (e.g., RESOURCE_NOT_FOUND) if the category ID is invalid.
     */
    fun getCategoryCodeById(categoryId: Long): String

    /**
     * Retrieves the unique identifier of the default category configured for the tenant.
     *
     * @return The ID of the default category.
     * @throws DomainException (e.g., RESOURCE_NOT_FOUND) if no default category is configured or found.
     */
    fun getDefaultCategoryId(): Long
}