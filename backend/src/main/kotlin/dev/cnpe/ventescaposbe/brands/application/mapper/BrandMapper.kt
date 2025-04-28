package dev.cnpe.ventescaposbe.brands.application.mapper

import dev.cnpe.ventescaposbe.brands.application.dto.response.BrandDetailedResponse
import dev.cnpe.ventescaposbe.brands.application.dto.response.BrandSummaryResponse
import dev.cnpe.ventescaposbe.brands.domain.model.Brand
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import org.springframework.stereotype.Component

@Component
class BrandMapper {


    fun toSummary(brand: Brand): BrandSummaryResponse {
        return BrandSummaryResponse(
            id = brand.id!!,
            name = brand.name,
            code = brand.code.codeValue,
            isDefault = brand.isDefault,
            createdAt = brand.createdAt
        )
    }


    fun toDetailed(brand: Brand): BrandDetailedResponse {
        return BrandDetailedResponse(
            id = brand.id!!,
            name = brand.name,
            code = brand.code.codeValue,
            isDefault = brand.isDefault,
            auditData = ResourceAuditData.fromBaseEntity(brand)
        )

    }

}