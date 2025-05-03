package dev.cnpe.ventescaposbe.promotions.application.service

import dev.cnpe.ventescaposbe.promotions.application.dto.request.CreateDiscountRuleRequest
import dev.cnpe.ventescaposbe.promotions.application.dto.response.DiscountRuleResponse
import dev.cnpe.ventescaposbe.promotions.application.mapper.DiscountRuleMapper
import dev.cnpe.ventescaposbe.promotions.infrastructure.persistence.DiscountRuleRepository
import dev.cnpe.ventescaposbe.shared.application.dto.PageResponse
import dev.cnpe.ventescaposbe.shared.application.exception.createDuplicatedResourceException
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


private val log = KotlinLogging.logger {}

@Service
@Transactional
class PromotionAdminService(
    private val discountRuleRepository: DiscountRuleRepository,
    private val discountRuleMapper: DiscountRuleMapper
) {

    /**
     * Creates a new discount rule based on the provided request details.
     *
     * @param request The request containing details for creating the discount rule, including name, type, value,
     *        applicability, and other configuration settings.
     * @return A response object representing the created discount rule, including its ID and other information.
     */
    fun createDiscountRule(request: CreateDiscountRuleRequest): DiscountRuleResponse {
        log.info { "Attempting to create discount rule: ${request.name}" }

        if (discountRuleRepository.findByName(request.name) != null) {
            throw createDuplicatedResourceException("name", request.name)
        }

        // TODO: Add validation here to check if targetProductIds, targetCategoryIds, targetBrandIds actually exist
        // using injected InfoPorts. This prevents creating rules pointing to non-existent entities.
        // Example: request.targetCategoryIds?.forEach { categoryInfoPort.getCategoryCodeById(it) }

        val entity = discountRuleMapper.toEntity(request)
        val savedEntity = discountRuleRepository.save(entity)

        log.info { "Discount rule created: ${savedEntity.name} (ID: ${savedEntity.id})" }
        return discountRuleMapper.toResponse(savedEntity)
    }

    /**
     * Retrieves the discount rule corresponding to the specified ID.
     *
     * @param id The unique identifier of the discount rule to fetch.
     * @return A `DiscountRuleResponse` object containing detailed information about the requested discount rule.
     */
    @Transactional(readOnly = true)
    fun getDiscountRule(id: Long): DiscountRuleResponse {
        log.debug { "Fetching discount rule with ID: $id" }
        val entity = discountRuleRepository.findByIdOrNull(id)
            ?: throw createResourceNotFoundException("DiscountRule", id)
        return discountRuleMapper.toResponse(entity)
    }

    /**
     * Retrieves a paginated list of discount rules.
     *
     * @param pageable The pagination and sorting information used to determine the page of results.
     * @return A PageResponse object containing DiscountRuleResponse objects along with pagination metadata.
     */
    @Transactional(readOnly = true)
    fun listDiscountRules(pageable: Pageable): PageResponse<DiscountRuleResponse> {
        log.debug { "Listing discount rules with pageable: $pageable" }
        // TODO: Add filtering capabilities later using Specifications
        val page = discountRuleRepository.findAll(pageable)
        val responsePage = page.map { discountRuleMapper.toResponse(it) }
        return PageResponse.from(responsePage)
    }

    /**
     * Updates the activation status of a discount rule based on its unique identifier.
     *
     * @param id The unique identifier of the discount rule to update.
     * @param isActive A flag indicating the desired activation status of the discount rule.
     *
     */
    fun updateDiscountRuleStatus(id: Long, isActive: Boolean): DiscountRuleResponse {
        log.info { "Updating status for discount rule ID: $id to isActive=$isActive" }
        val entity = discountRuleRepository.findByIdOrNull(id)
            ?: throw createResourceNotFoundException("DiscountRule", id)

        if (entity.isActive != isActive) {
            entity.isActive = isActive
            val savedEntity = discountRuleRepository.save(entity)
            log.info { "Discount rule ID: $id status updated to isActive=${savedEntity.isActive}" }
            return discountRuleMapper.toResponse(savedEntity)
        } else {
            log.info { "Discount rule ID: $id status already isActive=$isActive. No change." }
            return discountRuleMapper.toResponse(entity)
        }
    }

    // TODO: Implement full update method
    // fun updateDiscountRule(id: Long, request: UpdateDiscountRuleRequest): DiscountRuleResponse { ... }

    /**
     * Deletes a discount rule based on its unique identifier.
     *
     * @param id The unique identifier of the discount rule to delete.
     *           If the specified discount rule does not exist, a resource not found exception is thrown.
     */
    fun deleteDiscountRule(id: Long) {
        log.warn { "Attempting to delete discount rule ID: $id" }
        if (!discountRuleRepository.existsById(id)) {
            throw createResourceNotFoundException("DiscountRule", id)
        }
        // TODO: Add checks later if needed (e.g., cannot delete if currently applied to active order? idk if needed yet)
        discountRuleRepository.deleteById(id)
        log.info { "Discount rule ID: $id deleted." }
    }

}