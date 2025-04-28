package dev.cnpe.ventescaposbe.brands.application.service

import dev.cnpe.ventescaposbe.brands.application.api.BrandInfoPort
import dev.cnpe.ventescaposbe.brands.infrastructure.persistence.BrandRepository
import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode.RESOURCE_NOT_FOUND
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class BrandInfoAdapter(
    private val brandRepository: BrandRepository
) : BrandInfoPort {

    override fun getBrandCodeById(brandId: Long): String {
        log.debug { "Retrieving brand code for ID: $brandId" }
        return brandRepository.getBrandCodeById(brandId)
            ?: throw createResourceNotFoundException("Brand", brandId)
    }

    override fun getDefaultBrandId(): Long {
        log.debug { "Retrieving default brand ID" }
        val defaultBrand = brandRepository.getBrandByIsDefaultTrue()
            ?: run {
                log.debug { "No default brand found" }
                throw DomainException(
                    errorCode = RESOURCE_NOT_FOUND,
                    details = mapOf("reason" to "NO_DEFAULT_CATEGORY")
                )
            }
        return defaultBrand.id!!
    }
}