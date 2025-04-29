package dev.cnpe.ventescaposbe.categories.application.service

import dev.cnpe.ventescaposbe.categories.application.api.CategoryInfoPort
import dev.cnpe.ventescaposbe.categories.infrastructure.persistence.CategoryRepository
import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode.RESOURCE_NOT_FOUND
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class CategoryInfoAdapter(
    private val categoryRepository: CategoryRepository
) : CategoryInfoPort {

    override fun getCategoryCodeById(categoryId: Long): String {
        log.debug { "Fetching category code for ID: $categoryId" }
        return categoryRepository.getCategoryCodeById(categoryId)
            ?: throw createResourceNotFoundException("Category", categoryId)
    }

    override fun getDefaultCategoryId(): Long {
        log.debug { "Fetching default category ID" }
        val defaultCategory = (categoryRepository.getDefaultCategory()
            ?: run {
                log.debug { "No default category found." }
                throw DomainException(
                    errorCode = RESOURCE_NOT_FOUND,
                    details = mapOf("reason" to "NO_DEFAULT_CATEGORY")
                )
            })
        log.debug { "Found default category: ${defaultCategory.name} (ID: ${defaultCategory.id})" }
        return defaultCategory.id!!
    }

}