package dev.cnpe.ventescaposbe.customers.application.service

import dev.cnpe.ventescaposbe.business.application.api.BusinessDataPort
import dev.cnpe.ventescaposbe.customers.application.dto.request.CreateCustomerRequest
import dev.cnpe.ventescaposbe.customers.application.dto.request.UpdateCustomerRequest
import dev.cnpe.ventescaposbe.customers.application.dto.response.CustomerDetailedResponse
import dev.cnpe.ventescaposbe.customers.application.dto.response.CustomerSummaryResponse
import dev.cnpe.ventescaposbe.customers.application.mapper.CustomerMapper
import dev.cnpe.ventescaposbe.customers.domain.factory.CustomerFactory
import dev.cnpe.ventescaposbe.customers.domain.model.Customer
import dev.cnpe.ventescaposbe.customers.infrastructure.persistence.CustomerRepository
import dev.cnpe.ventescaposbe.shared.application.dto.PageResponse
import dev.cnpe.ventescaposbe.shared.application.exception.createDuplicatedResourceException
import dev.cnpe.ventescaposbe.shared.application.exception.createResourceNotFoundException
import dev.cnpe.ventescaposbe.shared.domain.vo.Address
import dev.cnpe.ventescaposbe.shared.domain.vo.PersonalInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val customerMapper: CustomerMapper,
    private val customerFactory: CustomerFactory,
    private val businessDataPort: BusinessDataPort
) {

    /** Creates a new customer with the provided information and returns a summary response */
    fun createCustomer(request: CreateCustomerRequest): CustomerSummaryResponse {
        log.debug { "Attempting to create customer: Name=${request.firstName} ${request.lastName ?: ""}, Email=${request.email ?: "N/A"}" }

        request.email?.takeIf { it.isNotBlank() }?.let {
            if (customerRepository.existsByEmail(it)) {
                throw createDuplicatedResourceException("email", it)
            }
        }
        request.taxId?.takeIf { it.isNotBlank() }?.let {
            if (customerRepository.existsByPersonalId(it)) {
                throw createDuplicatedResourceException("taxId", it)
            }
        }

        val defaultCurrency = businessDataPort.getBusinessPaymentData().currencyCode
        val address = Address.buildAddress(
            street = request.addressStreet,
            city = request.addressCity,
            zipCode = request.addressPostalCode,
            country = request.addressCountry
        )

        val newCustomer = customerFactory.create(
            firstName = request.firstName,
            lastName = request.lastName,
            taxId = request.taxId,
            email = request.email,
            phone = request.phone,
            address = address,
            notes = request.notes,
            defaultCurrency = defaultCurrency
        )

        val savedCustomer = customerRepository.save(newCustomer)
        log.info { "Customer created: ${savedCustomer.getFullName()} (ID: ${savedCustomer.id})" }
        return customerMapper.toSummary(savedCustomer)
    }

    /** Updates an existing customer's information and returns the detailed response */
    fun updateCustomer(customerId: Long, request: UpdateCustomerRequest): CustomerDetailedResponse {
        log.debug { "Attempting to update customer ID: $customerId" }
        val customer = findByIdOrThrow(customerId)

        val currentPersonalInfo = customer.personalInfo
        val updatedFirstName =
            request.firstName?.trim()?.replaceFirstChar { it.titlecase() } ?: currentPersonalInfo.firstName
        val updatedLastName = request.lastName?.trim()?.takeIf { it.isNotEmpty() }?.replaceFirstChar { it.titlecase() }
            ?: currentPersonalInfo.lastName
        val updatedTaxId =
            request.taxId?.trim()?.takeIf { it.isNotEmpty() } ?: currentPersonalInfo.personalId

        if (updatedTaxId != currentPersonalInfo.personalId && updatedTaxId != null) {
            if (customerRepository.existsByPersonalIdExcludingSelf(updatedTaxId, customerId)) {
                throw createDuplicatedResourceException("taxId", updatedTaxId)
            }
        }

        customer.personalInfo = currentPersonalInfo.copy(
            firstName = updatedFirstName,
            lastName = updatedLastName,
            personalId = updatedTaxId
        )

        request.email?.let { newEmail ->
            val trimmedEmail = newEmail.trim().takeIf { it.isNotEmpty() }
            if (trimmedEmail != customer.email) {
                if (trimmedEmail != null && customerRepository.existsByEmailAndIdNot(trimmedEmail, customerId)) {
                    throw createDuplicatedResourceException("email", trimmedEmail)
                }
                customer.email = trimmedEmail
            }
        }

        request.phone?.let { customer.phone = it.trim().takeIf { ph -> ph.isNotEmpty() } }

        val currentAddress = customer.address ?: Address.empty()
        val addressUpdated =
            request.addressStreet != null || request.addressCity != null || request.addressPostalCode != null || request.addressCountry != null
        if (addressUpdated) {
            customer.address = currentAddress.copy(
                street = request.addressStreet ?: currentAddress.street,
                city = request.addressCity ?: currentAddress.city,
                postalCode = request.addressPostalCode ?: currentAddress.postalCode,
                country = request.addressCountry ?: currentAddress.country
            )
        }

        request.notes?.let { customer.notes = it.trim().takeIf { n -> n.isNotEmpty() } }

        val savedCustomer = customerRepository.save(customer)
        log.info { "Customer updated: ${savedCustomer.getFullName()} (ID: ${savedCustomer.id})" }
        return customerMapper.toDetailed(savedCustomer)
    }

    /** Retrieves detailed information for a specific customer by ID */
    @Transactional(readOnly = true)
    fun getCustomerDetails(customerId: Long): CustomerDetailedResponse {
        log.debug { "Fetching details for customer ID: $customerId" }
        val customer = findByIdOrThrow(customerId)
        return customerMapper.toDetailed(customer)
    }

    /** Retrieves a paginated list of customers with optional search filtering */
    @Transactional(readOnly = true)
    fun listCustomers(pageable: Pageable, searchTerm: String?): PageResponse<CustomerSummaryResponse> {
        log.debug { "Listing customers with search: '$searchTerm', pageable: $pageable" }

        val spec = Specification<Customer> { root, query, cb ->
            searchTerm?.trim()?.takeIf { it.isNotEmpty() }?.let { term ->
                val pattern = "%${term.lowercase()}%"
                val firstNameMatch = cb.like(cb.lower(root.get<PersonalInfo>("personalInfo").get("firstName")), pattern)
                val lastNameMatch = cb.like(cb.lower(root.get<PersonalInfo>("personalInfo").get("lastName")), pattern)
                val emailMatch = cb.like(cb.lower(root.get("email")), pattern)
                val phoneMatch = cb.like(cb.lower(root.get("phone")), pattern)
                val taxIdMatch = cb.like(cb.lower(root.get<PersonalInfo>("personalInfo").get("personalId")), pattern)
                cb.or(firstNameMatch, lastNameMatch, emailMatch, phoneMatch, taxIdMatch)
            } ?: cb.conjunction()
        }

        val customerPage = customerRepository.findAll(spec, pageable)
        return PageResponse.from(customerPage.map(customerMapper::toSummary))
    }

    /** Activates a customer account and returns the updated customer details */
    fun activateCustomer(customerId: Long): CustomerDetailedResponse {
        log.info { "Activating customer ID: $customerId" }
        val customer = findByIdOrThrow(customerId)
        if (!customer.isActive) {
            customer.isActive = true
            customerRepository.save(customer)
            log.info { "Customer ${customer.id} activated." }
        } else {
            log.info { "Customer ${customer.id} is already active." }
        }
        return customerMapper.toDetailed(customer)
    }

    /** Deactivates a customer account and returns the updated customer details */
    fun deactivateCustomer(customerId: Long): CustomerDetailedResponse {
        log.warn { "Deactivating customer ID: $customerId" }
        val customer = findByIdOrThrow(customerId)
        if (customer.isActive) {
            customer.isActive = false
            customerRepository.save(customer)
            log.info { "Customer ${customer.id} deactivated." }
        } else {
            log.info { "Customer ${customer.id} is already inactive." }
        }
        return customerMapper.toDetailed(customer)
    }

    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun findByIdOrThrow(customerId: Long): Customer {
        return customerRepository.findByIdOrNull(customerId)
            ?: throw createResourceNotFoundException("Customer", customerId)
    }

}