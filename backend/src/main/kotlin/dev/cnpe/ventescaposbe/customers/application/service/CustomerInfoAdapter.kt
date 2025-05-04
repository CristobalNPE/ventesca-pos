package dev.cnpe.ventescaposbe.customers.application.service

import dev.cnpe.ventescaposbe.customers.application.api.CustomerInfoPort
import dev.cnpe.ventescaposbe.customers.application.api.dto.CustomerBasicInfo
import dev.cnpe.ventescaposbe.customers.application.mapper.CustomerMapper
import dev.cnpe.ventescaposbe.customers.infrastructure.persistence.CustomerRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class CustomerInfoAdapter(
    private val customerRepository: CustomerRepository,
    private val customerMapper: CustomerMapper
) : CustomerInfoPort {

    override fun getCustomerBasicInfo(customerId: Long): CustomerBasicInfo? {
        log.debug { "Port: Getting basic info for customer ID: $customerId" }
        return customerRepository.findById(customerId)
            .map { customerMapper.toBasicInfo(it) }
            .orElse(null)
    }

    override fun doesCustomerExist(customerId: Long): Boolean {
        log.debug { "Port: Checking existence for customer ID: $customerId" }
        val exists = customerRepository.existsById(customerId)
        log.trace { "Port: Customer ID $customerId exists: $exists" }
        return exists
    }
}