package dev.cnpe.ventescabekotlin.categories.application.mapper

import dev.cnpe.ventescabekotlin.categories.application.dto.response.CategoryCreatedResponse
import dev.cnpe.ventescabekotlin.categories.application.dto.response.CategoryDetailedResponse
import dev.cnpe.ventescabekotlin.categories.application.dto.response.CategorySummaryResponse
import dev.cnpe.ventescabekotlin.categories.application.dto.response.CategoryWithChildrenResponse
import dev.cnpe.ventescabekotlin.categories.domain.model.Category
import dev.cnpe.ventescabekotlin.shared.application.dto.ResourceAuditData
import org.springframework.stereotype.Component

@Component
class CategoryMapper {

    fun toSummary(category: Category, productCount: Long): CategorySummaryResponse {

        return CategorySummaryResponse(
            id = category.id!!,
            name = category.name,
            code = category.code.codeValue,
            color = category.color,
            isDefault = category.isDefault,
            productCount = productCount
        )
    }

    fun toDetailed(category: Category, productCount: Long): CategoryDetailedResponse {
        return CategoryDetailedResponse(
            id = category.id!!,
            name = category.name,
            code = category.code.codeValue,
            color = category.color,
            description = category.description,
            isRootCategory = category.isRootCategory(),
            isDefaultCategory = category.isDefault,
            parent = category.parent?.let { toSummary(it, productCount) },
            subcategories = category.subcategories.map { toSummary(it, productCount) }.toSet(),
            productCount = productCount,
            auditData = ResourceAuditData.fromBaseEntity(category)
        )

    }

    fun toCreated(category: Category): CategoryCreatedResponse {
        return CategoryCreatedResponse(
            id = category.id!!,
            name = category.name,
            code = category.code.codeValue,
            createdAt = category.createdAt
        )
    }

    fun toWithChildren(category: Category, productCount: Long): CategoryWithChildrenResponse {
        return CategoryWithChildrenResponse(
            id = category.id!!,
            name = category.name,
            code = category.code.codeValue,
            color = category.color,
            isRootCategory = category.isRootCategory(),
            isDefaultCategory = category.isDefault,
            productCount = productCount,
            subcategories = category.subcategories.map { toSummary(it, productCount) }.toSet()
        )
    }
}