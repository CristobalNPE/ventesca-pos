package dev.cnpe.ventescaposbe.catalog.api

import dev.cnpe.ventescaposbe.catalog.api.dto.ProductSaleInfo

/**
 * Port defining operations for retrieving basic Product information needed by other modules.
 */
interface ProductInfoPort {


    /**
     * Counts the number of products associated with a specific supplier ID.
     *
     * @param supplierId The ID of the supplier.
     * @return The number of products linked to the supplier.
     */
    fun countProductsBySupplierId(supplierId: Long): Long

    /**
     * Counts the number of products associated with a specific brand ID.
     *
     * @param brandId The ID of the brand.
     * @return The number of products linked to the brand.
     */
    fun countProductsByBrandId(brandId: Long): Long

    /**
     * Counts the number of products associated with a specific category ID.
     *
     * @param categoryId The ID of the category.
     * @return The number of products linked to the category.
     */
    fun countProductsByCategoryId(categoryId: Long): Long


    /**
     * Retrieves the sale information for a specific product based on its unique identifier.
     *
     * @param productId The unique identifier of the product whose sale information is to be retrieved.
     * @return A ProductSaleInfo object containing the product's sale status and current selling price.
     */
    fun getProductSaleInfo(productId:Long): ProductSaleInfo


    // TODO: Add methods for relocating products if needed (e.g., when deleting category/brand/supplier)
    // fun relocateProductsToDefaultSupplier(oldSupplierId: Long): Int

}