package dev.cnpe.ventescabekotlin.suppliers.application.service

import dev.cnpe.ventescabekotlin.business.event.BusinessActivatedEvent
import dev.cnpe.ventescabekotlin.shared.application.exception.createDuplicatedResourceException
import dev.cnpe.ventescabekotlin.shared.application.exception.createOperationNotAllowedException
import dev.cnpe.ventescabekotlin.shared.application.exception.createResourceNotFoundException
import dev.cnpe.ventescabekotlin.suppliers.application.dto.request.CreateSupplierRequest
import dev.cnpe.ventescabekotlin.suppliers.application.dto.request.UpdateSupplierRequest
import dev.cnpe.ventescabekotlin.suppliers.application.dto.request.UpdateSupplierStatusRequest
import dev.cnpe.ventescabekotlin.suppliers.application.dto.response.SupplierDetailedResponse
import dev.cnpe.ventescabekotlin.suppliers.application.dto.response.SupplierSummaryResponse
import dev.cnpe.ventescabekotlin.suppliers.application.events.SupplierDeleteAttemptedEvent
import dev.cnpe.ventescabekotlin.suppliers.application.exception.SupplierOperationNotAllowedReason.CANNOT_DEACTIVATE_DEFAULT_SUPPLIER
import dev.cnpe.ventescabekotlin.suppliers.application.exception.SupplierOperationNotAllowedReason.IS_DEFAULT_SUPPLIER
import dev.cnpe.ventescabekotlin.suppliers.application.mapper.SupplierMapper
import dev.cnpe.ventescabekotlin.suppliers.domain.Supplier
import dev.cnpe.ventescabekotlin.suppliers.domain.factory.SupplierFactory
import dev.cnpe.ventescabekotlin.suppliers.infrastructure.persistence.SupplierRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import dev.cnpe.ventescabekotlin.shared.application.exception.DomainException

private val log = KotlinLogging.logger {}

@Service
@Transactional
class SupplierService(
    private val supplierRepository: SupplierRepository,
    private val supplierMapper: SupplierMapper,
    private val supplierFactory: SupplierFactory,
    private val eventPublisher: ApplicationEventPublisher
) {

    /**
     * Registers a new supplier in the system.
     *
     * @param request The supplier creation details
     * @return A summary of the created supplier
     * @throws DomainException if a supplier with the same name already exists
     */
    fun registerSupplier(request: CreateSupplierRequest): SupplierSummaryResponse {
        log.debug { "Registering supplier with name: ${request.name}" }
        validateSupplierName(request.name)

        val newSupplier = supplierFactory.create(request.name)
        val savedSupplier = supplierRepository.save(newSupplier)

        log.info { "Supplier registered: ${savedSupplier.name} (ID: ${savedSupplier.id})" }
        return supplierMapper.toSummary(savedSupplier)
    }

    /**
     * Retrieves detailed information about a specific supplier.
     *
     * @param id The supplier's unique identifier
     * @return Detailed information about the supplier
     * @throws DomainException if the supplier is not found
     */
    @Transactional(readOnly = true)
    fun getSupplierDetails(id: Long): SupplierDetailedResponse {
        log.debug { "Fetching supplier details for ID: $id" }
        val supplier = findSupplierByIdOrThrow(id)
        return supplierMapper.toDetailed(supplier)
    }

    /**
     * Retrieves a list of all suppliers in the system.
     *
     * @return List of supplier summaries
     */
    @Transactional(readOnly = true)
    fun getAllSuppliers(): List<SupplierSummaryResponse> {
        log.debug { "Fetching all suppliers" }
        return supplierRepository.findAll().map { supplierMapper.toSummary(it) }
    }

    /**
     * Deletes a supplier from the system.
     *
     * @param id The supplier's unique identifier
     * @throws DomainException if the supplier is not found or if attempting to delete the default supplier
     */
    fun deleteSupplier(id: Long) {
        log.debug { "Attempting to delete supplier ID: $id" }
        val supplier = findSupplierByIdOrThrow(id)

        if (supplier.isDefault) {
            throw createOperationNotAllowedException(reason = IS_DEFAULT_SUPPLIER, entityId = supplier.id!!)
        }

        log.warn { "Deleting supplier: ${supplier.name} (ID: ${supplier.id})" }
        eventPublisher.publishEvent(SupplierDeleteAttemptedEvent(supplier.id!!))
        supplierRepository.delete(supplier)
        log.info { "Deleted supplier: ${supplier.name} (ID: ${supplier.id})" }
    }

    /**
     * Updates supplier information.
     *
     * @param id The supplier's unique identifier
     * @param request The update details
     * @return Updated supplier information
     * @throws DomainException if the supplier is not found
     * @throws DomainException if the new name conflicts with an existing supplier
     */
    fun updateSupplier(id: Long, request: UpdateSupplierRequest): SupplierDetailedResponse {
        log.debug { "Updating supplier with ID: $id with data: $request" }
        val supplier = findSupplierByIdOrThrow(id)

        updateSupplierFromRequest(supplier, request)

        val updatedSupplier = supplierRepository.save(supplier)
        log.info { "Supplier updated: ${updatedSupplier.name} (ID: ${updatedSupplier.id})" }
        return supplierMapper.toDetailed(updatedSupplier)
    }

    /**
     * Updates the active status of a supplier.
     *
     * @param id The supplier's unique identifier
     * @param request The status update details
     * @throws DomainException if the supplier is not found
     * @throws DomainException if attempting to deactivate the default supplier
     */
    fun updateSupplierStatus(id: Long, request: UpdateSupplierStatusRequest) {
        log.debug { "Updating status for supplier ID: $id to isActive=${request.isActive}" }
        val supplier = findSupplierByIdOrThrow(id)

        if (supplier.isDefault && !request.isActive) {
            throw createOperationNotAllowedException(CANNOT_DEACTIVATE_DEFAULT_SUPPLIER, supplier.id!!)
        }
        if (supplier.isActive != request.isActive) {
            supplier.isActive = request.isActive
            supplierRepository.save(supplier)
            log.info { "Supplier ID: $id status updated to isActive=${supplier.isActive}" }
        } else {
            log.info { "Supplier ID: $id status already isActive=${request.isActive}. No change." }
        }
    }

    /**
     * Handles business activation events by creating a default supplier if none exists.
     *
     * @param event The business activation event
     */
    @ApplicationModuleListener
    fun onBusinessActivated(event: BusinessActivatedEvent) {
        log.debug { "Received BusinessActivatedEvent for supplier check: ${event.businessName}" }
        if (supplierRepository.existsDefaultSupplier()) {
            log.info { "Default supplier already exists." }
            return
        }

        val defaultSupplierName = "${event.businessName} (Autoabastecimiento)" //TODO: i18n?
        log.info { "Creating default self-supplier: $defaultSupplierName" }

        val defaultSupplier = supplierFactory.create(defaultSupplierName).apply {
            isDefault = true
            isActive = true
        }
        val saved = supplierRepository.save(defaultSupplier)
        log.info { "🚚 Created default supplier: ${saved.name} (ID: ${saved.id})" }
    }


    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun findSupplierByIdOrThrow(id: Long): Supplier {
        return supplierRepository.findByIdOrNull(id)
            ?: throw createResourceNotFoundException("Supplier", id)
    }

    private fun validateSupplierName(name: String) {
        if (supplierRepository.existsByName(name)) {
            throw createDuplicatedResourceException("name", name)
        }
    }

    private fun updateSupplierFromRequest(supplier: Supplier, request: UpdateSupplierRequest) {
        request.name?.let { newName ->
            if (supplier.name != newName && supplierRepository.existsByName(newName)) {
                throw createDuplicatedResourceException("name", newName)
            }
            supplier.name = newName
        }

        val currentPersonalInfo = supplier.representativeInfo
        val updatedPersonalInfo = currentPersonalInfo.copy(
            firstName = request.representativeFirstName ?: currentPersonalInfo.firstName,
            lastName = request.representativeLastName ?: currentPersonalInfo.lastName,
            personalId = request.representativePersonalId ?: currentPersonalInfo.personalId
        )
        if (updatedPersonalInfo != currentPersonalInfo) {
            supplier.representativeInfo = updatedPersonalInfo
        }

        val currentContactInfo = supplier.contactInfo
        val updatedContactInfo = currentContactInfo.copy(
            phoneNumber = request.phoneNumber ?: currentContactInfo.phoneNumber,
            email = request.email ?: currentContactInfo.email,
            website = request.website ?: currentContactInfo.website
        )
        if (updatedContactInfo != currentContactInfo) {
            supplier.contactInfo = updatedContactInfo
        }

        val currentAddress = supplier.address
        val updatedAddress = currentAddress.copy(
            street = request.addressStreet ?: currentAddress.street,
            city = request.addressCity ?: currentAddress.city,
            country = request.addressCountry ?: currentAddress.country,
            postalCode = request.addressPostalCode ?: currentAddress.postalCode
        )
        if (updatedAddress != currentAddress) {
            supplier.address = updatedAddress
        }
    }
}