package dev.cnpe.ventescaposbe.suppliers.application.mapper

import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import dev.cnpe.ventescaposbe.suppliers.application.dto.response.SupplierDetailedResponse
import dev.cnpe.ventescaposbe.suppliers.application.dto.response.SupplierSummaryResponse
import dev.cnpe.ventescaposbe.suppliers.domain.Supplier
import org.springframework.stereotype.Component

@Component
class SupplierMapper {

    /**
     * Transforms a Supplier entity into a SupplierSummaryResponse, providing a summary view of the supplier.
     *
     * @param supplier the Supplier entity containing detailed supplier information
     * @return a SupplierSummaryResponse with basic summary details of the supplier
     */
    fun toSummary(supplier: Supplier): SupplierSummaryResponse {
        return SupplierSummaryResponse(
            id = supplier.id!!,
            name = supplier.name,
            isActive = supplier.isActive,
            isDefault = supplier.isDefault,
            createdAt = supplier.createdAt
        )
    }

    /**
     * Converts a Supplier entity into a detailed response representation.
     *
     * @param supplier the Supplier entity containing detailed supplier information
     * @return a SupplierDetailedResponse object with complete details about the supplier
     */
    fun toDetailed(supplier: Supplier): SupplierDetailedResponse {
        return SupplierDetailedResponse(
            id = supplier.id!!,
            name = supplier.name,
            representative = supplier.representativeInfo,
            contactInfo = supplier.contactInfo,
            address = supplier.address,
            isActive = supplier.isActive,
            auditData = ResourceAuditData.fromBaseEntity(supplier)
        )
    }

}