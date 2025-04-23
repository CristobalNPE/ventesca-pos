package dev.cnpe.ventescabekotlin.categories.application.service

import dev.cnpe.ventescabekotlin.categories.application.dto.request.CreateCategoryRequest
import dev.cnpe.ventescabekotlin.categories.application.dto.request.UpdateCategoryRequest
import dev.cnpe.ventescabekotlin.categories.application.dto.response.CategoryCreatedResponse
import dev.cnpe.ventescabekotlin.categories.application.dto.response.CategoryDetailedResponse
import dev.cnpe.ventescabekotlin.categories.application.dto.response.CategoryWithChildrenResponse
import dev.cnpe.ventescabekotlin.categories.application.events.CategoryRelocatedEvent
import dev.cnpe.ventescabekotlin.categories.application.exception.CategoryOperationNotAllowedReason.*
import dev.cnpe.ventescabekotlin.categories.application.mapper.CategoryMapper
import dev.cnpe.ventescabekotlin.categories.domain.factory.CategoryFactory
import dev.cnpe.ventescabekotlin.categories.domain.model.Category
import dev.cnpe.ventescabekotlin.categories.infrastructure.CategoryRepository
import dev.cnpe.ventescabekotlin.shared.application.exception.createDuplicatedResourceException
import dev.cnpe.ventescabekotlin.shared.application.exception.createOperationNotAllowedException
import dev.cnpe.ventescabekotlin.shared.application.exception.createResourceNotFoundException
import dev.cnpe.ventescabekotlin.shared.application.service.CodeGeneratorService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val MAX_CATEGORY_DEPTH: Int = 2
private val log = KotlinLogging.logger {}

@Service
@Transactional
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val categoryMapper: CategoryMapper,
    private val categoryFactory: CategoryFactory,
    private val eventPublisher: ApplicationEventPublisher,
    private val codeGeneratorService: CodeGeneratorService
) {

    @Transactional(readOnly = true)
    fun getCategoryDetails(id: Long): CategoryDetailedResponse {
        val category = findCategoryByIdOrThrow(id)
        val productCount = 666L // TODO: Replace with productInfoPort.countProductsByCategoryId(id)
//        val productCount: Long = productInfoPort.countProductsByCategoryId(id)

        return categoryMapper.toDetailed(category, productCount)
    }

    fun deleteCategory(id: Long) {
        log.debug { "Attempting to delete category for ID: $id" }
        val category = findCategoryByIdOrThrow(id)

        validateDeletion(category)
        relocateProducts(category)


        log.info { "Deleting category: ${category.name} (ID: ${category.id})" }
        categoryRepository.delete(category)
        log.info { "Deleted category: ${category.name} (ID: ${category.id})" }
    }

    private fun relocateProducts(category: Category) {
        val defaultCategory = (categoryRepository.getDefaultCategory()
            ?: throw IllegalStateException("Default category not found during product relocation"))

        val targetCategoryId = if (category.isRootCategory()) {
            defaultCategory.id!!
        } else {
            category.parent!!.id!!
        }
        log.info {
            "Relocating products from category ${category.name} " +
                    "(ID: ${category.id}) to category ${defaultCategory.name} " +
                    "(ID: ${defaultCategory.id}) before deletion"
        }
        eventPublisher.publishEvent(CategoryRelocatedEvent(targetCategoryId))
    }


    fun createCategory(request: CreateCategoryRequest): CategoryCreatedResponse {
        log.debug { "Attempting to create category: ${request.name}" }
        validateCategory(request)

        val newCategory = categoryFactory.create(request.name)
        val created = categoryRepository.save(newCategory)
        log.info { "Created category: ${created.name} (ID: ${created.id})" }

        return categoryMapper.toCreated(created)
    }

    fun updateCategory(id: Long, request: UpdateCategoryRequest): CategoryDetailedResponse {
        log.debug { "Attempting to update category for ID: $id with data: $request" }
        val category = findCategoryByIdOrThrow(id)
        val productCount = 0L // TODO

        updateCategoryFromRequest(category, request)
        val updatedCategory = categoryRepository.save(category)
        log.info { "Updated category: ${updatedCategory.name} (ID: ${updatedCategory.id})" }
        return categoryMapper.toDetailed(updatedCategory, productCount)

    }


    fun addSubcategory(request: CreateCategoryRequest, parentId: Long): CategoryCreatedResponse {
        log.debug { "Attempting to add subcategory: ${request.name} to parent ID: $parentId" }
        val parent = findCategoryByIdOrThrow(parentId)

        validateParentCategory(parent)
        validateCategoryDepth(parent)
        validateCategory(request)

        val subcategory = categoryFactory.createSubcategory(request.name, parent)
        val savedSubcategory = categoryRepository.save(subcategory)

        log.info { "Created subcategory: ${savedSubcategory.name} (ID: ${savedSubcategory.id}) under parent ${parent.name}" }
        return categoryMapper.toCreated(savedSubcategory)
    }

    private fun validateParentCategory(parent: Category) {
        if (parent.isDefault) {
            throw createOperationNotAllowedException(
                reason = ADD_SUBCATEGORY_TO_DEFAULT,
                entityId = parent.id!!,
                additionalDetails = mapOf("parentName" to parent.name)
            )
        }
    }


    @Transactional(readOnly = true)
    fun getAllCategories(): List<CategoryWithChildrenResponse> {
        log.debug { "Fetching all categories" }
        val productCount = 0L // TODO: Replace with productInfoPort calls
        return categoryRepository.findAllParentCategories().map { categoryMapper.toWithChildren(it, productCount) }
    }

    private fun findCategoryByIdOrThrow(id: Long): Category {
        log.debug { "Fetching category details for ID: $id" }
        return categoryRepository.findByIdOrNull(id)
            ?: throw createResourceNotFoundException("Category", id)
    }

    private fun updateCategoryFromRequest(category: Category, request: UpdateCategoryRequest) {
        request.name?.let { newName ->
            if (category.name != newName && categoryRepository.existsByName(newName)) {
                throw createDuplicatedResourceException("name", request.name)
            }
            category.name = newName
            category.updateCode(codeGeneratorService.generateCode(newName))
            log.trace { "Updated category ID ${category.id} name to '$newName' and regenerated code" }
        }
        request.description?.let { category.description = it }
        request.color?.let { newColor ->
            // TODO: Optionally update children colors recursively? Requires loading children.
            // category.subcategories.forEach { child -> child.color = categoryFactory.generateSubcategoryColor(newColor) }
            log.trace { "Updated category ID ${category.id} color to '$newColor'" }
        }
    }

    private fun validateCategoryDepth(parent: Category) {
        var depth = 0
        var current: Category? = parent
        while (current != null) {
            current = current.parent
            depth++
        }

        val resultingDepth = depth + 1

        if (resultingDepth > MAX_CATEGORY_DEPTH) {
            throw createOperationNotAllowedException(
                reason = REACHED_MAX_DEPTH,
                entityId = parent.id!!,
                additionalDetails = mapOf(
                    "maxAllowedDepth" to MAX_CATEGORY_DEPTH,
                    "attemptedDepth" to resultingDepth,
                    "parentName" to parent.name,
                )
            )
        }
    }

    private fun validateDeletion(category: Category) {
        if (category.isDefault) {
            throw createOperationNotAllowedException(
                reason = IS_DEFAULT_CATEGORY,
                entityId = category.id!!,
                additionalDetails = mapOf("categoryName" to category.name)
            )
        }

        // TODO: Add check using ProductInfoPort
        // val productCount = productInfoPort.countProductsByCategoryId(category.id!!)
        // if (productCount > 0) {
        //     throw DomainException(OPERATION_NOT_ALLOWED, "Cannot delete category with associated products ($productCount)")
        // }
        if (category.subcategories.isNotEmpty()) {
            throw createOperationNotAllowedException(
                reason = HAS_SUBCATEGORIES,
                entityId = category.id!!,
                additionalDetails = mapOf(
                    "categoryName" to category.name,
                    "subCategoryCount" to category.subcategories.size
                )
            )
        }
    }

    private fun validateCategory(request: CreateCategoryRequest) {
        if (categoryRepository.existsByName(request.name)) {
            throw createDuplicatedResourceException("name", request.name)
        }
    }

}