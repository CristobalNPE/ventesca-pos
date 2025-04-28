package dev.cnpe.ventescaposbe.suppliers.application.service

import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode.RESOURCE_NOT_FOUND
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import dev.cnpe.ventescaposbe.suppliers.application.api.SupplierInfoPort
import dev.cnpe.ventescaposbe.suppliers.infrastructure.persistence.SupplierRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class SupplierInfoAdapter(
    private val supplierRepository: SupplierRepository
) : SupplierInfoPort {

    override fun getDefaultSupplierId(): Long {
        log.debug { "Attempting to find default supplier ID" }
        val defaultSupplier = supplierRepository.getSupplierByIsDefaultIsTrue()
            ?: run {
                log.warn { "Default supplier not found in the database." }
                throw DomainException(RESOURCE_NOT_FOUND, details = mapOf("defaultSupplier" to "true"))
            }

        log.info { "Found default supplier ID: ${defaultSupplier.id!!}" }
        return defaultSupplier.id!!
    }

    override fun getSupplierNameById(id: Long): String {
        log.debug { "Fetching supplier name for ID: $id" }
        val supplier = supplierRepository.findByIdOrNull(id)
            ?: run {
                log.warn { "Supplier not found for ID: $id" }
                throw createResourceNotFoundException("Supplier", id)
            }

        log.trace { "Found supplier name '${supplier.name}' for ID: $id" }
        return supplier.name
    }
}