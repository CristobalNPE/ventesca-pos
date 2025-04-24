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

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        try {
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication != null && authentication.isAuthenticated && authentication is JwtAuthenticationToken) {
                log.debug { "JWT Authentication found. Populating user context." }
                userContext.userId = authentication.token.subject
                userContext.email = authentication.token.getClaimAsString("email")
                userContext.preferredUsername = authentication.token.getClaimAsString("preferred_username")
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
            .map { if (it.startsWith("ROLE_")) it.substring(5) else it }
            .toSet()
    }

    // TODO: Define paths this filter should NOT apply to (similar to TenantAuthenticationFilter)
    // override fun shouldNotFilter(request: HttpServletRequest): Boolean { ... }
}