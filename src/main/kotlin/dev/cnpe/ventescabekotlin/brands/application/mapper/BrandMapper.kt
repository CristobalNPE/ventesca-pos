package dev.cnpe.ventescabekotlin.brands.application.mapper

import dev.cnpe.ventescabekotlin.brands.application.dto.response.BrandDetailedResponse
import dev.cnpe.ventescabekotlin.brands.application.dto.response.BrandSummaryResponse
import dev.cnpe.ventescabekotlin.brands.domain.model.Brand
import dev.cnpe.ventescabekotlin.shared.application.dto.ResourceAuditData
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