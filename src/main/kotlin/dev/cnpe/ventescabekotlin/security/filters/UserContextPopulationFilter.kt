package dev.cnpe.ventescabekotlin.security.filters

import dev.cnpe.ventescabekotlin.security.context.RequestUserContext
import dev.cnpe.ventescabekotlin.tenant.TenantContext
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class UserContextPopulationFilter(
    private val userContext: RequestUserContext
) : OncePerRequestFilter() {

    companion object {
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
            } else {
                log.trace { "No authenticated JWT found, UserContext remains empty." }
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to populate UserContext" }
        }
        filterChain.doFilter(request, response)
    }

    private fun extractRoles(authorities: Collection<GrantedAuthority>): Set<String> {
        return authorities
            .map { it.authority }
            .map { if (it.startsWith(ROLE_PREFIX)) it.substring(ROLE_PREFIX.length) else it }
            .toSet()
    }

    // TODO: Define paths this filter should NOT apply to (similar to TenantAuthenticationFilter)
    // override fun shouldNotFilter(request: HttpServletRequest): Boolean { ... }
}