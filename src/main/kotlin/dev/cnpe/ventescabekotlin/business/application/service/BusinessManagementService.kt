package dev.cnpe.ventescabekotlin.business.application.service

import dev.cnpe.ventescabekotlin.business.application.dto.request.CreateBusinessBranchRequest
import dev.cnpe.ventescabekotlin.business.application.dto.request.UpdateBusinessBasicsRequest
import dev.cnpe.ventescabekotlin.business.application.dto.request.UpdateBusinessConfigurationRequest
import dev.cnpe.ventescabekotlin.business.application.dto.request.UpdateBusinessContactInfoRequest
import dev.cnpe.ventescabekotlin.business.application.dto.response.BusinessBranchInfo
import dev.cnpe.ventescabekotlin.business.application.dto.response.BusinessDetailedResponse
import dev.cnpe.ventescabekotlin.business.application.dto.response.BusinessStatusResponse
import dev.cnpe.ventescabekotlin.business.application.mapper.BusinessMapper
import dev.cnpe.ventescabekotlin.business.domain.enums.BusinessStatus
import dev.cnpe.ventescabekotlin.business.domain.model.Business
import dev.cnpe.ventescabekotlin.business.domain.model.BusinessBranch
import dev.cnpe.ventescabekotlin.business.infrastructure.persistence.BusinessRepository
import dev.cnpe.ventescabekotlin.business.infrastructure.persistence.BusinessUserRepository
import dev.cnpe.ventescabekotlin.security.context.UserContext
import dev.cnpe.ventescabekotlin.shared.application.exception.DomainException
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode.INSUFFICIENT_CONTEXT
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode.RESOURCE_NOT_FOUND
import dev.cnpe.ventescabekotlin.shared.domain.vo.Address
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}


/**
 * Service responsible for operations performed by a Business Admin on their own business.
 * Requires TenantContext to be set correctly.
 */
@Service
@Transactional
open class BusinessManagementService(
    private val businessFactory: BusinessFactory,
    private val businessRepository: BusinessRepository,
    private val businessUserRepository: BusinessUserRepository,
    private val businessMapper: BusinessMapper,
    private val eventPublisher: ApplicationEventPublisher,
    private val userContext: UserContext,
    private val messageSource: MessageSource
) {

    /**
     * Retrieves detailed data for the business associated with the currently logged-in user.
     */
    @Transactional(readOnly = true)
    fun getCurrentUserBusinessData(): BusinessDetailedResponse {
        val business = getCurrentUserBusinessOrThrow()
        return businessMapper.toDetailedDto(business)
    }




    /**
     * Updates the basic details (name, brand message) of the current user's business.
     */
    fun updateBasics(request: UpdateBusinessBasicsRequest) {
        val business = getCurrentUserBusinessOrThrow()
        log.debug { "Updating basics for business ID: ${business.id}" }

        val updatedDetails = businessFactory.buildBusinessDetails(request, business.details)
        if (updatedDetails != business.details) {
            business.details = updatedDetails
            businessRepository.save(business)
            log.info { "Updated business basics for ID: ${business.id}" }
        } else {
            log.info { "No changes detected in business basics for ID: ${business.id}" }
        }
    }

    /**
     * Updates the contact information of the current user's business.
     */
    fun updateContactInfo(request: UpdateBusinessContactInfoRequest) {
        val business = getCurrentUserBusinessOrThrow()
        log.debug { "Updating contact info for business ID: ${business.id}" }

        val newContactInfo = businessFactory.buildBusinessContactInfo(request)
        business.contactInfo = newContactInfo
        businessRepository.save(business)
        log.info { "Updated business contact info for ID: ${business.id}" }
    }

    /**
     * Updates the configuration (currency, tax, payment methods) of the current user's business.
     */
    fun updateBusinessConfiguration(request: UpdateBusinessConfigurationRequest) {
        val business = getCurrentUserBusinessOrThrow()
        log.debug { "Updating configuration for business ID: ${business.id}" }

        val newConfig = businessFactory.buildBusinessConfig(request)
        business.configuration = newConfig
        // TODO: Add validation? E.g., ensure currency code exists in Currency table?
        businessRepository.save(business)
        log.info { "Updated business configuration for ID: ${business.id}" }
    }

    /**
     * Creates a new (non-main) branch for the current user's business.
     *
     * @param request DTO containing details for the new branch.
     * @return DTO representation of the newly created branch.
     * @throws DomainException if required input is missing or invalid.
     */
    fun registerBranch(request: CreateBusinessBranchRequest): BusinessBranchInfo {
        val business = getCurrentUserBusinessOrThrow()

        val managerId = request.managerId ?: userContext.userId ?: throw DomainException(
            INSUFFICIENT_CONTEXT,
            message = "User ID not available for branch manager"
        )
        log.info { "Registering new branch for business ID ${business.id} with data: $request" }

        val newBranchAddress = Address.buildAddress(
            street = request.addressStreet,
            city = request.addressCity,
            country = request.addressCountry,
            zipCode = request.addressZipCode
        ) ?: Address.empty()

        val newBranch = BusinessBranch(
            business = business,
            branchName = request.branchName ?: "PLACEHOLDER", //TODO use messageSource
            address = newBranchAddress,
            isMainBranch = false,
            branchManagerId = managerId,
            branchContactNumber = request.contactNumber
        )

        newBranch.business = business
        business.addBranch(newBranch)

        businessRepository.save(business)
        log.info { "Successfully registered new branch '${newBranch.branchName}' (ID: ${newBranch.id}) for business ID ${business.id}" }
        val savedBranch = business.branches.find { it === newBranch }
            ?: throw IllegalStateException("Saved branch not found in collection immediately after save")

        return businessMapper.mapBranchToInfoDto(savedBranch)
    }

    /**
     * Designates an existing branch as the main branch for the business.
     * The previously main branch (if any) will be unset.
     *
     * @param branchId The ID of the branch to set as the new main branch.
     * @throws DomainException if the branch is not found, already main, or doesn't belong to the business.
     */
    fun setMainBranch(branchId: Long) {
        val business = getCurrentUserBusinessOrThrow()
        log.info { "Attempting to set branch ID $branchId as main branch for business ID ${business.id}" }

        val currentMainBranch = business.getMainBranch()
        val targetBranch = business.branches.find { it.id == branchId } ?: throw DomainException(
            RESOURCE_NOT_FOUND,
            mapOf("entityType" to "Target Branch", "branchId" to branchId)
        )

        if (targetBranch === currentMainBranch) {
            log.warn { "Branch ID $branchId is already the main branch for business ID ${business.id}. No change needed." }
            return
        }
        // extra check (maybe not needed)
        require(targetBranch.business == business) { "Branch $branchId does not belong to business ${business.id}" }

        log.info { "Changing main branch for business ${business.id} from ${currentMainBranch?.id ?: "None"} to $branchId" }
        currentMainBranch?.isMainBranch = false
        targetBranch.isMainBranch = true

        businessRepository.save(business)
        log.info { "Successfully set branch ID $branchId as main branch for business ID ${business.id}" }
    }


    /**
     * Gets the status overview for the business associated with the current user.
     * Queries the MASTER database to check existence before potentially querying tenant.
     */
    @Transactional(readOnly = true, transactionManager = "masterTransactionManager") // Query master first
    fun getCurrentBusinessStatus(): BusinessStatusResponse {
        val currentUserIdpId = userContext.userId
            ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "User ID not available")

        val status = businessUserRepository.findBusinessStatusByIdpUserId(currentUserIdpId)

        return if (status != null) {
            val isSetupComplete = status != BusinessStatus.PENDING
            BusinessStatusResponse(status, false, isSetupComplete)
        } else {
            BusinessStatusResponse(BusinessStatus.NON_CREATED, needsCreation = true, isSetupComplete = false)
        }
    }


    // *******************************
    // 🔰 Private Helpers
    // *******************************

    /**
     * Retrieves the Business entity associated with the current logged-in user.
     * Uses UserContext and queries across tenant/master boundaries implicitly via repositories.
     * Throws if the user context or business link is missing.
     */
    @Transactional(readOnly = true)
    protected fun getCurrentUserBusinessOrThrow(): Business {
        val currentUserIdpId = userContext.userId
            ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "User ID not available")

        val businessUser = businessUserRepository.findByIdpUserId(currentUserIdpId)
            ?: throw DomainException(
                RESOURCE_NOT_FOUND,
                mapOf("entityType" to "BusinessUserLink", "idpUserId" to currentUserIdpId)
            )

        return businessUser.business
            ?: throw DomainException(
                RESOURCE_NOT_FOUND,
                mapOf("entityType" to "Business", "userIdpId" to currentUserIdpId)
            )
    }
}