package dev.cnpe.ventescabekotlin.business.application.service

import dev.cnpe.ventescabekotlin.business.application.dto.request.AdminCreateBusinessRequest
import dev.cnpe.ventescabekotlin.business.application.dto.request.CreateBusinessBranchRequest
import dev.cnpe.ventescabekotlin.business.application.dto.request.UpdateBusinessBasicsRequest
import dev.cnpe.ventescabekotlin.business.application.dto.request.UpdateBusinessConfigurationRequest
import dev.cnpe.ventescabekotlin.business.application.dto.request.UpdateBusinessContactInfoRequest
import dev.cnpe.ventescabekotlin.business.domain.enums.BusinessStatus
import dev.cnpe.ventescabekotlin.business.domain.enums.PaymentMethod
import dev.cnpe.ventescabekotlin.business.domain.model.Business
import dev.cnpe.ventescabekotlin.business.domain.model.BusinessBranch
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessConfiguration
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessContactInfo
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessDetails
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessStatusInfo
import dev.cnpe.ventescabekotlin.shared.domain.vo.Address
import dev.cnpe.ventescabekotlin.tenant.service.TenantManagementService
import dev.cnpe.ventescabekotlin.tenant.vo.TenantIdentifier
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.OffsetDateTime

private val log = KotlinLogging.logger {}

/**
 * Helper component responsible for creating initial Business entities and related VOs/Branches.
 * Used by AdminBusinessService and BusinessManagementService.
 */
@Component
class BusinessFactory(
    private val tenantManagementService: TenantManagementService
) {
    /**
     * Creates the initial, unsaved Business entity shell when a new business is registered by an Admin.
     * This includes creating the tenant database and schema.
     * The calling service is responsible for populating details, config, user links, etc., and saving.
     *
     * @param adminUserId The IdP User ID ('sub') of the designated business administrator.
     * @param initialBusinessName The initial name for the business (used for tenant ID generation).
     * @return A Pair containing the newly created TenantIdentifier and the unsaved Business entity shell.
     * @throws TenantCreationException If tenant DB/schema creation fails.
     */
    fun createNewBusinessShell(adminUserId: String, initialBusinessName: String): Pair<TenantIdentifier, Business> {
        require(adminUserId.isNotBlank()) { "Admin User ID cannot be blank" }
        require(initialBusinessName.isNotBlank()) { "Initial Business Name cannot be blank" }

        log.info { "Initiating creation for business '$initialBusinessName' for admin '$adminUserId'" }

        val tenantId = tenantManagementService.createTenant(initialBusinessName)

        val initialStatusInfo = BusinessStatusInfo(
            status = BusinessStatus.PENDING,
            reason = "Initial business registration",
            changedAt = OffsetDateTime.now()
        )

        val businessShell = Business(
            adminId = adminUserId,
            tenantId = tenantId,
            details = BusinessDetails(businessName = initialBusinessName, logoUrl = null, brandMessage = null),
            contactInfo = null,
            configuration = null,
            statusInfo = initialStatusInfo
        )

        log.info { "Created business shell for ${tenantId.value}" }
        return Pair(tenantId, businessShell)
    }

    fun buildBusinessDetails(request: UpdateBusinessBasicsRequest, currentDetails: BusinessDetails): BusinessDetails {
        return currentDetails.copy(
            businessName = request.businessName ?: currentDetails.businessName,
            brandMessage = request.brandMessage ?: currentDetails.brandMessage,
            // TODO: logo url update on separate handler (file upload service call)
        )
    }

    fun buildBusinessDetails(request: AdminCreateBusinessRequest): BusinessDetails {
        return BusinessDetails(
            businessName = request.businessName,
            brandMessage = request.brandMessage,
            logoUrl = null //set later
        )
    }

    fun buildBusinessContactInfo(request: UpdateBusinessContactInfoRequest): BusinessContactInfo {
        return BusinessContactInfo(
            phone = request.phone ?: "",
            email = request.email,
            website = request.website
        )
    }

    fun buildBusinessContactInfo(request: AdminCreateBusinessRequest): BusinessContactInfo? {
        return if (!request.contactPhone.isNullOrBlank()) {
            BusinessContactInfo(
                phone = request.contactPhone,
                email = request.contactEmail,
                website = request.contactWebsite
            )
        } else null
    }

    fun buildBusinessConfig(request: UpdateBusinessConfigurationRequest): BusinessConfiguration {
        return BusinessConfiguration(
            currencyCode = request.currencyCode ?: "CLP",
            taxPercentage = request.taxPercentage ?: BigDecimal.ZERO,
            acceptedPaymentMethods = request.acceptedPaymentMethods ?: setOf(PaymentMethod.CASH)
        )
    }

    fun buildBusinessConfig(request: AdminCreateBusinessRequest): BusinessConfiguration {
        return BusinessConfiguration(
            currencyCode = request.currencyCode,
            taxPercentage = request.taxPercentage,
            acceptedPaymentMethods = request.acceptedPaymentMethods
        )
    }

    fun buildInitialMainBranch(request: AdminCreateBusinessRequest, adminUserId: String): BusinessBranch? {
        val address = Address.buildAddress(
            street = request.mainBranchStreet,
            city = request.mainBranchCity,
            zipCode = request.mainBranchZipCode,
            country = request.mainBranchCountry
        )

        // only create branch if address could be created:
        return address?.let { addr ->
            BusinessBranch(
                business = TODO("Placeholder: Business instance needed here - set by calling service"),
                branchName = request.mainBranchName ?: "Main Office",  // TODO: handle default naming via MessageSource in service layer
                address = addr,
                isMainBranch = true,
                branchManagerId = adminUserId,
                branchContactNumber = request.contactPhone
            )
        }

    }

    fun buildBusinessBranch(request: CreateBusinessBranchRequest, managerId: String): BusinessBranch {
        val address = Address.buildAddress(
            street = request.addressStreet,
            city = request.addressCity,
            zipCode = request.addressZipCode,
            country = request.addressCountry
        ) ?: Address.empty() // Ensure address is not null for branch

        return BusinessBranch(
            business = TODO("Placeholder: Business instance needed here - set by calling service"),
            branchName = request.branchName ?: "New Branch", // TODO: handle default naming via MessageSource in service layer
            address = address,
            isMainBranch = false,
            branchManagerId = managerId,
            branchContactNumber = request.contactNumber
        )
    }

}