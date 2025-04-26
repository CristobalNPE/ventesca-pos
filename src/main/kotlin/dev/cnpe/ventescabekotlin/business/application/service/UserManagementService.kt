package dev.cnpe.ventescabekotlin.business.application.service

import dev.cnpe.ventescabekotlin.business.application.dto.response.BusinessUserInfo
import dev.cnpe.ventescabekotlin.business.application.exception.BusinessOperationNotAllowedReason.*
import dev.cnpe.ventescabekotlin.business.config.BusinessLimitProperties
import dev.cnpe.ventescabekotlin.business.domain.model.BusinessUser
import dev.cnpe.ventescabekotlin.business.infrastructure.persistence.BusinessRepository
import dev.cnpe.ventescabekotlin.business.infrastructure.persistence.BusinessUserRepository
import dev.cnpe.ventescabekotlin.security.context.UserContext
import dev.cnpe.ventescabekotlin.security.ports.IdentityProviderPort
import dev.cnpe.ventescabekotlin.security.ports.dto.NewUserData
import dev.cnpe.ventescabekotlin.security.ports.dto.UserIdentity
import dev.cnpe.ventescabekotlin.shared.application.exception.DomainException
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode.INSUFFICIENT_CONTEXT
import dev.cnpe.ventescabekotlin.shared.application.exception.createDuplicatedResourceException
import dev.cnpe.ventescabekotlin.shared.application.exception.createOperationNotAllowedException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

private val log = KotlinLogging.logger {}

@Service
open class UserManagementService(
    private val idpPort: IdentityProviderPort,
    private val userContext: UserContext,
    private val businessUserRepository: BusinessUserRepository,
    private val businessRepository: BusinessRepository,
    @Qualifier("masterTransactionTemplate") private val masterTransactionTemplate: TransactionTemplate,
    private val businessLimitProperties: BusinessLimitProperties
) {
    companion object {
        // roles that can be assigned by a Business Admin
        private val ALLOWED_ROLES_TO_ASSIGN = setOf("SELLER", "BRANCH_MANAGER")
        private const val TENANT_ID_ATTRIBUTE = "tenant_id"
        private const val ROLE_SELLER = "SELLER"
        private const val ROLE_BRANCH_MANAGER = "BRANCH_MANAGER"
    }

    /**
     * Creates a new user (Seller or Branch Manager) within the current Business Admin's tenant.
     */
    @Transactional(propagation = Propagation.NEVER)
    fun createBusinessUser(request: NewUserData, rolesToAssign: Set<String>): UserIdentity {
        val adminUserId = userContext.userId
            ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "Admin User ID missing")
        val adminTenantId = userContext.tenantId
            ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "Admin Tenant ID missing")

        log.info { "Business Admin [$adminUserId] attempting to create user [${request.email}] in tenant [$adminTenantId] with roles $rolesToAssign" }

        validateUserCreationRequest(request, rolesToAssign)

        val idpAttributes = mapOf(TENANT_ID_ATTRIBUTE to adminTenantId)
        val newUserIdp = idpPort.createUser(request, rolesToAssign, idpAttributes)
        log.info { "IdP user ${newUserIdp.email} created with ID ${newUserIdp.id} for tenant $adminTenantId" }

        masterTransactionTemplate.execute {

            val business = businessRepository.findByTenantIdValue(adminTenantId)
                ?: throw IllegalStateException("Cannot link user ${newUserIdp.id} - Business not found in master DB for tenant $adminTenantId")

            val displayName = "${newUserIdp.firstName ?: ""} ${newUserIdp.lastName ?: ""}"
                .trim().ifEmpty { newUserIdp.username ?: newUserIdp.email }

            val businessUserLink = BusinessUser.createLink(
                idpUserId = newUserIdp.id,
                userEmail = request.email,
                displayName = displayName,
                roles = rolesToAssign
            )
            businessUserLink.business = business
            businessUserRepository.save(businessUserLink)
            log.info { "BusinessUser link saved for IdP User ${newUserIdp.id} to Tenant $adminTenantId" }
        } ?: throw IllegalStateException("Failed to save business user link within transaction.")

        return newUserIdp
    }

    /**
     * Assigns/updates roles for a specific user within the admin's tenant.
     * Ensures admin can only assign allowed roles and only to users in their tenant.
     */
    @Transactional(propagation = Propagation.NEVER)
    fun assignBusinessUserRoles(targetUserIdpId: String, rolesToAssign: Set<String>) {
        val adminUserId = userContext.userId
            ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "Admin User ID missing")
        val adminTenantId = userContext.tenantId
            ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "Admin Tenant ID missing")

        log.info { "Admin [$adminUserId] attempting to assign roles $rolesToAssign to user [$targetUserIdpId] in tenant [$adminTenantId]" }

        val invalidRoles = rolesToAssign - ALLOWED_ROLES_TO_ASSIGN
        if (invalidRoles.isNotEmpty()) {
            throw createOperationNotAllowedException(
                CANNOT_ASSIGN_ROLES,
                additionalDetails = mapOf("requestedRoles" to rolesToAssign, "invalidRoles" to invalidRoles)
            )
        }

        verifyUserTenantAffiliation(targetUserIdpId, adminTenantId)

        // FIXME: idpPort.assignRolesToUser currently ADDS roles. If we need to REPLACE,
        // the adapter implementation needs to fetch existing roles, remove them, then add new ones.
        idpPort.assignRolesToUser(targetUserIdpId, rolesToAssign)
        log.info { "Successfully assigned roles $rolesToAssign to user [$targetUserIdpId] by admin [$adminUserId]" }
    }

    /**
     * Deletes a user associated with the admin's tenant from both the IdP
     * and the local BusinessUser link table.
     */
    @Transactional(propagation = Propagation.NEVER)
    fun deleteBusinessUser(targetUserIdpId: String) {
        val adminUserId = userContext.userId
            ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "Admin User ID missing")
        val adminTenantId = userContext.tenantId
            ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "Admin Tenant ID missing")

        log.warn { "Admin [$adminUserId] attempting to DELETE user [$targetUserIdpId] in tenant [$adminTenantId]" }

        verifyUserTenantAffiliation(targetUserIdpId, adminTenantId)

        idpPort.deleteUser(targetUserIdpId)
        log.info { "Successfully deleted user [$targetUserIdpId] from IdP by admin [$adminUserId]" }

        masterTransactionTemplate.execute {
            val deletedCount = businessUserRepository.deleteByIdpUserId(targetUserIdpId)
            if (deletedCount > 0) {
                log.info { "Successfully deleted BusinessUser link for IdP User [$targetUserIdpId] from master DB." }
            } else {
                // inconsistency
                log.warn { "Could not find BusinessUser link for IdP User [$targetUserIdpId] to delete, though user existed in IdP." }
            }
        }
        log.info { "Successfully completed deletion process for user [$targetUserIdpId] initiated by admin [$adminUserId]" }
    }

    /**
     * Lists users associated with the current Business Admin's tenant.
     * Fetches user links from master DB and potentially basic info from IdP.
     */
    @Transactional(readOnly = true, transactionManager = "masterTransactionManager")
    fun listBusinessUsers(): List<BusinessUserInfo> {
        val adminTenantId =
            userContext.tenantId ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "Admin Tenant ID missing")

        log.debug { "Listing users for tenant [$adminTenantId]" }

        val business = businessRepository.findByTenantIdValue(adminTenantId)
            ?: throw DomainException(
                GeneralErrorCode.RESOURCE_NOT_FOUND,
                mapOf("entityType" to "Business", "tenantId" to adminTenantId)
            )

        val userLinks = businessUserRepository.findAllByBusinessId(business.id!!)

        // Consider if the BusinessUser table should store name/email snapshot.
        return userLinks.map { userLink ->
            BusinessUserInfo(
                idpUserId = userLink.idpUserId,
                email = userLink.userEmail,
                displayName = userLink.displayName ?: userLink.userEmail ?: userLink.idpUserId,
                roles = userLink.roles //placeholder
            )
        }
    }

    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun verifyUserTenantAffiliation(targetUserIdpId: String, expectedTenantId: String) {

    }

    private fun validateUserCreationRequest(request: NewUserData, rolesToAssign: Set<String>) {

        val adminTenantId = userContext.tenantId
            ?: throw DomainException(INSUFFICIENT_CONTEXT, message = "Admin Tenant ID missing")

        if (!userContext.roles.contains("BUSINESS_ADMIN")) {
            throw createOperationNotAllowedException(NOT_AUTHORIZED_TO_MANAGE_USERS)
        }

        // 2. Check if email already exists in IdP
        idpPort.findUserByEmail(request.email)?.let {
            throw createDuplicatedResourceException("email", request.email)
        }

        // 3. Check if roles being assigned are valid for a Business Admin to assign
        val invalidRoles = rolesToAssign - ALLOWED_ROLES_TO_ASSIGN
        if (invalidRoles.isNotEmpty()) {
            throw createOperationNotAllowedException(CANNOT_ASSIGN_ROLES)
        }

        // 4. Limit checks
        val business = businessRepository.findByTenantIdValue(adminTenantId)
            ?: throw DomainException(
                errorCode = INSUFFICIENT_CONTEXT,
                message = "Could not find business details for admin's tenant $adminTenantId during validation"
            )

        val businessId = business.id!!

        val currentTotalUserCount = businessUserRepository.countByBusinessId(businessId)
        val potentialTotalUsers = currentTotalUserCount + 1

        if (potentialTotalUsers > businessLimitProperties.maxUsersPerBusiness) {
            throw createOperationNotAllowedException(
                reason = USER_LIMIT_REACHED,
                additionalDetails = mapOf("maxAllowed" to businessLimitProperties.maxUsersPerBusiness)
            )
        }


        log.debug { "User creation request validated successfully." }
    }

    // TODO: Implement method for update user (initiated by Business Admin)
    // These method MUST include checks to ensure the admin is operating only on users
    // belonging to their OWN tenant (by checking the tenant_id attribute via idpPort.getUserAttributes).
    // see: delete/list
}