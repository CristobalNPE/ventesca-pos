package dev.cnpe.ventescabekotlin.brands.application.service

import dev.cnpe.ventescabekotlin.brands.application.dto.request.CreateBrandRequest
import dev.cnpe.ventescabekotlin.brands.application.dto.request.UpdateBrandRequest
import dev.cnpe.ventescabekotlin.brands.application.dto.response.BrandDetailedResponse
import dev.cnpe.ventescabekotlin.brands.application.dto.response.BrandSummaryResponse
import dev.cnpe.ventescabekotlin.brands.application.events.BrandDeleteAttemptedEvent
import dev.cnpe.ventescabekotlin.brands.application.mapper.BrandMapper
import dev.cnpe.ventescabekotlin.brands.domain.factory.BrandFactory
import dev.cnpe.ventescabekotlin.brands.domain.model.Brand
import dev.cnpe.ventescabekotlin.brands.infrastructure.BrandRepository
import dev.cnpe.ventescabekotlin.business.event.BusinessActivatedEvent
import dev.cnpe.ventescabekotlin.shared.application.service.CodeGeneratorService
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

    fun registerBrand(brandRequest: CreateBrandRequest): BrandSummaryResponse {
        log.debug { "Attempting to register brand: ${brandRequest.name}" }
//        validateBrandRequest(brandRequest)

        val newBrand = brandFactory.create(brandRequest.name)
        val savedBrand = brandRepository.save(newBrand)
        log.info { "Registered brand: ${savedBrand.name} (ID: ${savedBrand.id})" }

        return brandMapper.toSummary(savedBrand)
    }

    @Transactional(readOnly = true)
    fun getBrandDetails(id: Long): BrandDetailedResponse {
        val brand = findBrandByIdOrThrow(id)
        return brandMapper.toDetailed(brand)
    }

    fun updateBrand(id: Long, brandRequest: UpdateBrandRequest): BrandDetailedResponse {
        log.debug { "Attempting to update brand for ID: $id with data: $brandRequest" }
        val brand = findBrandByIdOrThrow(id)

        updateBrandFromRequest(brand, brandRequest)

        val updatedBrand = brandRepository.save(brand)
        log.info { "Updated brand: ${updatedBrand.name} (ID: ${updatedBrand.id})" }

        return brandMapper.toDetailed(updatedBrand)
    }

    @Transactional(readOnly = true)
    fun getAllBrands(): List<BrandSummaryResponse> {
        log.debug { "Fetching all brands" }
        return brandRepository.findAll().map { brandMapper.toSummary(it) }
    }

    fun deleteBrand(id: Long) {
        log.debug { "Attempting to delete brand for ID: $id" }
        val brand = findBrandByIdOrThrow(id)

        if (brand.isDefault) {
            throw RuntimeException("Cannot delete the default brand.")
//            throw DomainException(OPERATION_NOT_ALLOWED, "Cannot delete the default brand.")
        }
        // TODO: Add check for product dependencies using ProductInfoPort when migrated
        log.warn { "Deleting brand: ${brand.name} (ID: ${brand.id})" }
        eventPublisher.publishEvent(BrandDeleteAttemptedEvent(brand.id!!))
        brandRepository.delete(brand)
        log.info { "Deleted brand: ${brand.name} (ID: ${brand.id})" }
    }

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

    private fun updateBrandFromRequest(brand: Brand, brandRequest: UpdateBrandRequest) {
        brandRequest.name?.let { newName ->
            if (brand.name != newName && brandRepository.existsByName(newName)) {
                throw RuntimeException("Brand name '$newName' already exists.")
//                throw DomainException(DUPLICATED_DATA, "name '$newName'")
            }
            brand.name = newName
            brand.updateCodeValue(codeGeneratorService.generateCode(newName))
            log.trace { "Updated brand ID ${brand.id} name to '$newName' and regenerated code" }
        }
    }

    private fun validateBrandRequest(brandRequest: CreateBrandRequest) {
        if (brandRepository.existsByName(brandRequest.name)) {
            throw RuntimeException("Brand name '${brandRequest.name}' already exists.")
//            throw DomainException(DUPLICATED_DATA, "name '${brandRequest.name}'")
        }
    }

    private fun findBrandByIdOrThrow(id: Long): Brand {
        log.debug { "Fetching brand details for ID: $id" }
        val brand = brandRepository.findByIdOrNull(id)
            ?: throw RuntimeException("Brand not found for ID: $id")
        //  ?: throw DomainException(RESOURCE_NOT_FOUND, id.toString())

        return brand
    }

}