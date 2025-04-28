package dev.cnpe.ventescaposbe.brands.application.service

import dev.cnpe.ventescaposbe.brands.application.dto.request.CreateBrandRequest
import dev.cnpe.ventescaposbe.brands.application.dto.request.UpdateBrandRequest
import dev.cnpe.ventescaposbe.brands.application.dto.response.BrandDetailedResponse
import dev.cnpe.ventescaposbe.brands.application.dto.response.BrandSummaryResponse
import dev.cnpe.ventescaposbe.brands.application.events.BrandDeleteAttemptedEvent
import dev.cnpe.ventescaposbe.brands.application.exception.BrandOperationNotAllowedReason.IS_DEFAULT_BRAND
import dev.cnpe.ventescaposbe.brands.application.mapper.BrandMapper
import dev.cnpe.ventescaposbe.brands.domain.factory.BrandFactory
import dev.cnpe.ventescaposbe.brands.domain.model.Brand
import dev.cnpe.ventescaposbe.brands.infrastructure.persistence.BrandRepository
import dev.cnpe.ventescaposbe.business.event.BusinessActivatedEvent
import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.createDuplicatedResourceException
import dev.cnpe.ventescaposbe.shared.application.exception.createOperationNotAllowedException
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import dev.cnpe.ventescaposbe.shared.application.service.CodeGeneratorService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional
class BrandService(
    private val brandRepository: BrandRepository,
    private val brandMapper: BrandMapper,
    private val brandFactory: BrandFactory,
    private val eventPublisher: ApplicationEventPublisher,
    private val codeGeneratorService: CodeGeneratorService
) {

    // *******************************
    // 🔰 Brand Management
    // *******************************

    /**
     * Registers a new brand based on the provided request, validates the data,
     * and persists the brand into the repository. Converts the newly created
     * brand entity into a summary response.
     *
     * @param brandRequest The request containing the brand details to create, including a mandatory name.
     * @return A summary response object containing details of the newly created brand.
     * @throws DomainException If a brand with the same name already exists.
     */
    fun registerBrand(brandRequest: CreateBrandRequest): BrandSummaryResponse {
        log.debug { "Attempting to register brand: ${brandRequest.name}" }
        validateBrandRequest(brandRequest)

        val newBrand = brandFactory.create(brandRequest.name)
        val savedBrand = brandRepository.save(newBrand)
        log.info { "Registered brand: ${savedBrand.name} (ID: ${savedBrand.id})" }

        return brandMapper.toSummary(savedBrand)
    }

    /**
     * Retrieves detailed information about a brand based on its unique identifier.
     *
     * @param id The ID of the brand to retrieve details for.
     * @return A detailed response object containing information about the specified brand.
     * @throws DomainException If the brand with the given ID does not exist.
     */
    @Transactional(readOnly = true)
    fun getBrandDetails(id: Long): BrandDetailedResponse {
        val brand = findBrandByIdOrThrow(id)
        return brandMapper.toDetailed(brand)
    }

    /**
     * Updates an existing brand based on the provided brand ID and update request data.
     *
     * @param id The ID of the brand to update.
     * @param brandRequest The request containing the updated brand details.
     * @return A detailed response object containing information about the updated brand.
     * @throws DomainException If the update request fails validation or if a brand with the requested name already exists.
     */
    fun updateBrand(id: Long, brandRequest: UpdateBrandRequest): BrandDetailedResponse {
        log.debug { "Attempting to update brand for ID: $id with data: $brandRequest" }
        val brand = findBrandByIdOrThrow(id)

        updateBrandFromRequest(brand, brandRequest)

        val updatedBrand = brandRepository.save(brand)
        log.info { "Updated brand: ${updatedBrand.name} (ID: ${updatedBrand.id})" }

        return brandMapper.toDetailed(updatedBrand)
    }

    /**
     * Retrieves a list of all brands and maps them to summary response objects.
     *
     * @return A list of BrandSummaryResponse objects representing all brands.
     */
    @Transactional(readOnly = true)
    fun getAllBrands(): List<BrandSummaryResponse> {
        log.debug { "Fetching all brands" }
        return brandRepository.findAll().map { brandMapper.toSummary(it) }
    }

    /**
     * Deletes a brand identified by the provided ID.
     * If the brand is marked as a default brand, an exception is thrown.
     * Also, emits an event indicating an attempt to delete the brand.
     *
     * @param id The unique identifier of the brand to be deleted.
     * @throws DomainException If the brand is a default brand or does not exist.
     */
    fun deleteBrand(id: Long) {
        log.debug { "Attempting to delete brand for ID: $id" }
        val brand = findBrandByIdOrThrow(id)

        if (brand.isDefault) {
            throw createOperationNotAllowedException(
                reason = IS_DEFAULT_BRAND,
                entityId = brand.id!!,
                additionalDetails = mapOf("brandName" to brand.name)
            )
        }
        // TODO: Add check for product dependencies using ProductInfoPort when migrated
        log.warn { "Deleting brand: ${brand.name} (ID: ${brand.id})" }
        eventPublisher.publishEvent(BrandDeleteAttemptedEvent(brand.id!!))
        brandRepository.delete(brand)
        log.info { "Deleted brand: ${brand.name} (ID: ${brand.id})" }
    }

    /**
     * Handles the activation of a business by reacting to the BusinessActivatedEvent.
     * If no brands exist, a default brand is created and persisted in the repository.
     * The default brand is named after the business with a suffix "(Marca Propia)" and a unique code is generated for it.
     *
     * @param event The BusinessActivatedEvent containing information about the activated business such as business ID and name.
     */
    @ApplicationModuleListener
    fun onBusinessActivated(event: BusinessActivatedEvent) {
        log.debug { "Received BusinessActivatedEvent for: ${event.businessId}" }

        if (brandRepository.count() > 0) {
            log.info { "Brand already exists, skipping default brand creation." }
            return
        }

        val defaultBrand = brandFactory.createDefault(event.businessName)
        defaultBrand.name = "${event.businessName} (Marca Propia)"
        defaultBrand.updateCodeValue(codeGeneratorService.generateCode(defaultBrand.name))

        val saved = brandRepository.save(defaultBrand)
        log.info { "Created default brand: ${saved.name} (ID: ${saved.id})" }
    }

    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun updateBrandFromRequest(brand: Brand, brandRequest: UpdateBrandRequest) {
        brandRequest.name.let { newName ->
            if (brand.name != newName && brandRepository.existsByName(newName)) {
                throw createDuplicatedResourceException("name", brandRequest.name)
            }
            brand.name = newName
            brand.updateCodeValue(codeGeneratorService.generateCode(newName))
            log.trace { "Updated brand ID ${brand.id} name to '$newName' and regenerated code" }
        }
    }

    private fun validateBrandRequest(brandRequest: CreateBrandRequest) {
        if (brandRepository.existsByName(brandRequest.name)) {
            throw createDuplicatedResourceException("name", brandRequest.name)
        }
    }

    private fun findBrandByIdOrThrow(id: Long): Brand {
        log.debug { "Fetching brand details for ID: $id" }
        val brand = brandRepository.findByIdOrNull(id)
            ?: throw createResourceNotFoundException("Brand", id)

        return brand
    }
}