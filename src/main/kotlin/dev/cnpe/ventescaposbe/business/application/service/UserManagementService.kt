package dev.cnpe.ventescaposbe.business.application.service

import dev.cnpe.ventescaposbe.business.application.dto.response.BusinessUserInfo
import dev.cnpe.ventescaposbe.business.application.exception.BusinessOperationNotAllowedReason.*
import dev.cnpe.ventescaposbe.business.config.BusinessLimitProperties
import dev.cnpe.ventescaposbe.business.domain.model.BusinessUser
import dev.cnpe.ventescaposbe.business.infrastructure.persistence.BusinessRepository
import dev.cnpe.ventescaposbe.business.infrastructure.persistence.BusinessUserRepository
import dev.cnpe.ventescaposbe.security.context.UserContext
import dev.cnpe.ventescaposbe.security.exception.IdpAccessException
import dev.cnpe.ventescaposbe.security.exception.IdpUserNotFoundException
import dev.cnpe.ventescaposbe.security.ports.IdentityProviderPort
import dev.cnpe.ventescaposbe.security.ports.dto.NewUserData
import dev.cnpe.ventescaposbe.security.ports.dto.UserIdentity
import dev.cnpe.ventescaposbe.shared.application.exception.DomainException
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode
import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode.INSUFFICIENT_CONTEXT
import dev.cnpe.ventescaposbe.shared.application.exception.createDuplicatedResourceException
import dev.cnpe.ventescaposbe.shared.application.exception.createOperationNotAllowedException
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

        try {
            idpPort.deleteUser(targetUserIdpId)
            log.info { "Successfully deleted user [$targetUserIdpId] from IdP by admin [$adminUserId]" }
        } catch (e: IdpUserNotFoundException) {
            log.warn { "User [$targetUserIdpId] not found in IdP during deletion attempt, but proceeding to check local link." }
        } catch (e: Exception) {
            log.error(e) { "Error deleting user [$targetUserIdpId] from IdP." }
            throw e
        }

        masterTransactionTemplate.execute {
            val deletedCount = businessUserRepository.deleteByIdpUserId(targetUserIdpId)
            if (deletedCount > 0) {
                log.info { "Successfully deleted BusinessUser link for IdP User [$targetUserIdpId] from master DB." }
            } else {
                log.warn { "Could not find BusinessUser link for IdP User [$targetUserIdpId] to delete." }
            }
        } ?: throw IllegalStateException("Failed to execute master transaction for BusinessUser link deletion.")

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

    /**
     * Verifies that the target user belongs to the expected tenant.
     * Fetches user attributes from the IdP and compares the 'tenant_id' attribute.
     *
     * @param targetUserIdpId The IdP ID of the user being acted upon.
     * @param expectedTenantId The tenant ID the user is expected to belong to (usually the admin's tenant).
     * @throws DomainException(OPERATION_NOT_ALLOWED) if the user does not belong to the expected tenant or tenant info is missing.
     * @throws IdpUserNotFoundException if the target user doesn't exist in the IdP.
     * @throws IdpAccessException for communication errors.
     */
    private fun verifyUserTenantAffiliation(targetUserIdpId: String, expectedTenantId: String) {
        log.debug { "Verifying tenant affiliation for user [$targetUserIdpId]. Expected tenant: [$expectedTenantId]" }

        try {
            val attributes = idpPort.getUserAttributes(targetUserIdpId)
            val userTenantIdList = attributes[TENANT_ID_ATTRIBUTE]

            if (userTenantIdList.isNullOrEmpty()) {
                log.error { "Target user [$targetUserIdpId] is missing the '$TENANT_ID_ATTRIBUTE' attribute. Cannot verify affiliation." }
                throw createOperationNotAllowedException(
                    TARGET_USER_TENANT_MISMATCH,
                    entityId = targetUserIdpId,
                    additionalDetails = mapOf("reason" to "Target user missing tenant attribute")
                )
            }
            val userTenantId = userTenantIdList.first()

            if (userTenantId != expectedTenantId) {
                log.error { "Tenant mismatch for user [$targetUserIdpId]. Expected: [$expectedTenantId], Found: [$userTenantId]. Operation forbidden." }
                throw createOperationNotAllowedException(
                    TARGET_USER_TENANT_MISMATCH,
                    entityId = targetUserIdpId,
                    additionalDetails = mapOf(
                        "reason" to "Target user belongs to a different tenant",
                        "expectedTenant" to expectedTenantId,
                        "actualTenant" to userTenantId
                    )
                )
            }

            log.info { "✅ User [$targetUserIdpId] affiliation verified for tenant [$expectedTenantId]." }
        } catch (e: IdpUserNotFoundException) {
            log.error { "User [$targetUserIdpId] not found in IdP during tenant affiliation check." }
            throw e
        } catch (e: IdpAccessException) {
            log.error(e) { "IdP access error during tenant affiliation check for user [$targetUserIdpId]." }
            throw e
        } catch (e: Exception) {
            log.error(e) { "Unexpected error during tenant affiliation check for user [$targetUserIdpId]." }
            throw DomainException(
                GeneralErrorCode.GENERAL,
                message = "Failed to verify user tenant affiliation",
                cause = e
            )
        }


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