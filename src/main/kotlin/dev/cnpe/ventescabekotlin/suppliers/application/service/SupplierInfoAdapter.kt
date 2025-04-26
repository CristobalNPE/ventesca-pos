package dev.cnpe.ventescabekotlin.suppliers.application.service

import dev.cnpe.ventescabekotlin.shared.application.exception.DomainException
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode.RESOURCE_NOT_FOUND
import dev.cnpe.ventescabekotlin.suppliers.application.api.SupplierInfoPort
import dev.cnpe.ventescabekotlin.suppliers.infrastructure.persistence.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SupplierInfoAdapter(
    private val supplierRepository: SupplierRepository
) : SupplierInfoPort {

    override fun getDefaultSupplierId(): Long {
        val defaultSupplier = (supplierRepository.getSupplierByDefaultIsTrue()
            ?: throw DomainException(RESOURCE_NOT_FOUND, details = mapOf("defaultSupplier" to "true")))

        return defaultSupplier.id!!
    }
}