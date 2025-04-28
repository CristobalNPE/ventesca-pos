package dev.cnpe.ventescaposbe.catalog.application.validation

import dev.cnpe.ventescaposbe.brands.application.api.BrandInfoPort
import dev.cnpe.ventescaposbe.categories.application.api.CategoryInfoPort
import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode.*
import dev.cnpe.ventescaposbe.suppliers.application.api.SupplierInfoPort
import org.springframework.stereotype.Component

@Component
class ProductRelationValidator(
    private val categoryInfoPort: CategoryInfoPort,
    private val brandInfoPort: BrandInfoPort,
    private val supplierInfoPort: SupplierInfoPort
) {

    fun validateRelations(categoryId: Long?, brandId: Long?, supplierId: Long?) {
        validateRelationExists(categoryInfoPort::getCategoryCodeById, categoryId, "Category")
        validateRelationExists(brandInfoPort::getBrandCodeById, brandId, "Brand")
        validateRelationExists(supplierInfoPort::getSupplierNameById, supplierId, "Supplier")
    }

    /**
     * Validates if a related entity with the given ID exists by calling the provided fetcher function.
     * Throws an INVALID_DATA DomainException if the entity is not found or the fetcher fails.
     *
     * @param fetcher A function (lambda or method reference) that takes a Long ID and returns Any? (or throws).
     * @param id The ID of the entity to validate.
     * @param entityTypeName The user-friendly name of the entity type (e.g., "Category", "Brand").
     * @throws DomainException if validation fails.
     */
    private fun validateRelationExists(fetcher: (Long) -> Any?, id: Long?, entityTypeName: String) {
        if (id == null) {
            throw DomainException(INVALID_DATA, details = mapOf("field" to "${entityTypeName}Id"))
        }
        try {
            fetcher(id) ?: throw DomainException(
                INVALID_DATA,
                details = mapOf("field" to "${entityTypeName}Id", "value" to id),
                message = "Invalid $entityTypeName ID: $id"
            )
        } catch (e: DomainException) {
            if (e.errorCode == RESOURCE_NOT_FOUND) {
                throw DomainException(
                    INVALID_DATA,
                    details = mapOf("field" to "${entityTypeName}Id", "value" to id),
                    cause = e
                )
            } else {
                throw e
            }
        } catch (e: Exception) {
            throw DomainException(GENERAL, message = "Error validating $entityTypeName ID $id", cause = e)
        }
    }
}