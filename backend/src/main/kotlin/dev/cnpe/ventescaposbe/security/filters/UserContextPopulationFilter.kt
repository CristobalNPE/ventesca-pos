package dev.cnpe.ventescaposbe.security.filters

import dev.cnpe.ventescaposbe.business.infrastructure.persistence.BusinessBranchRepository
import dev.cnpe.ventescaposbe.business.infrastructure.persistence.BusinessUserRepository
import dev.cnpe.ventescaposbe.security.context.RequestUserContext
import dev.cnpe.ventescaposbe.tenant.TenantContext
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

@Component
class UserContextPopulationFilter(
    private val userContext: RequestUserContext,
    private val businessUserRepository: BusinessUserRepository,
    private val businessBranchRepository: BusinessBranchRepository
) : OncePerRequestFilter() {

    companion object {
        private const val ROLE_BUSINESS_ADMIN = "BUSINESS_ADMIN"
        private const val CLAIM_EMAIL = "email"
        private const val CLAIM_PREFERRED_USERNAME = "preferred_username"
        private const val CLAIM_REALM_ACCESS = "realm_access"
        private const val CLAIM_ROLES = "roles"
        private const val ROLE_PREFIX = "ROLE_"
        private val IGNORED_ROLES = setOf("offline_access", "uma_authorization")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        log.trace { "Entering UserContextPopulationFilter for: ${request.requestURI}" }

        try {
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication != null && authentication.isAuthenticated && authentication is JwtAuthenticationToken) {
                val token = authentication.token
                log.debug { "JWT Authentication found. Populating user context." }

                userContext.userId = token.subject
                userContext.email = token.getClaimAsString(CLAIM_EMAIL)
                userContext.preferredUsername = token.getClaimAsString(CLAIM_PREFERRED_USERNAME)
                userContext.roles = extractRoles(authentication.authorities)
                userContext.tenantId = TenantContext.getCurrentTenant()

                populateAllowedBranchIds()

                log.debug { // TODO: change to trace later
                    """
                        User Context populated with 
                        userId: ${userContext.userId}, 
                        email: ${userContext.email}, 
                        tenantId: ${userContext.tenantId}, 
                        preferredUsername: ${userContext.preferredUsername}, 
                        roles: ${userContext.roles}, 
                        allowedBranchIds: ${userContext.allowedBranchIds}
                    """.trimIndent()
                }
            } else {
                log.trace { "No authenticated JWT found, UserContext remains empty." }
                clearUserContext()
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to populate UserContext" }
            clearUserContext()
        }
        filterChain.doFilter(request, response)
        log.trace { "Exiting UserContextPopulationFilter for: ${request.requestURI}" }
    }

    private fun populateAllowedBranchIds() {
        if (userContext.userId != null) {
            try {
                if (userContext.roles.contains(ROLE_BUSINESS_ADMIN)) {
                    log.debug { "User ${userContext.userId} is BUSINESS_ADMIN. Fetching all branches." }
                    val businessUser = businessUserRepository.findByIdpUserId(userContext.userId!!)
                    val businessId = businessUser?.business?.id

                    if (businessId != null) {
                        userContext.allowedBranchIds =
                            businessBranchRepository.findAllIdsByBusinessId(businessId)
                        log.debug { "Admin allowed branches (all for business $businessId): ${userContext.allowedBranchIds}" }
                    } else {
                        log.warn { "Could not find business associated with admin ${userContext.userId}. Cannot determine allowed branches." }
                        userContext.allowedBranchIds = emptySet()
                    }
                } else {
                    userContext.allowedBranchIds =
                        businessUserRepository.findAssignedBranchIdsByIdpUserId(userContext.userId!!)
                    log.debug { "Fetched assigned branches for user ${userContext.userId}: ${userContext.allowedBranchIds}" }
                }
            } catch (e: Exception) {
                log.error(e) { "Failed to fetch allowed/assigned branches for user ${userContext.userId}" }
                userContext.allowedBranchIds = null
            }
        } else {
            userContext.allowedBranchIds = null
        }
    }

    private fun extractRoles(authorities: Collection<GrantedAuthority>): Set<String> {
        return authorities
            .map { it.authority }
            .map { if (it.startsWith(ROLE_PREFIX)) it.substring(ROLE_PREFIX.length) else it }
            .toSet()
    }

    private fun clearUserContext() {
        log.debug { "Clearing user context." }
        userContext.userId = null
        userContext.email = null
        userContext.preferredUsername = null
        userContext.roles = emptySet()
        userContext.tenantId = null
        userContext.allowedBranchIds = null
    }

    // TODO: Define paths this filter should NOT apply to (similar to TenantAuthenticationFilter)
    // override fun shouldNotFilter(request: HttpServletRequest): Boolean { ... }
}