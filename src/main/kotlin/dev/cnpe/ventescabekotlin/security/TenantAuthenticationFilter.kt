package dev.cnpe.ventescabekotlin.security

import dev.cnpe.ventescabekotlin.business.BusinessDataPort
import dev.cnpe.ventescabekotlin.tenant.TenantContext
import dev.cnpe.ventescabekotlin.tenant.exception.TenantNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

@Component
class TenantAuthenticationFilter(
    private val businessDataPort: BusinessDataPort
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var tenantIdSet: String? = null

        try {
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication != null && authentication.isAuthenticated && authentication is JwtAuthenticationToken) {
                val userEmail = authentication.token.getClaimAsString("email")

                if (userEmail.isNullOrBlank()) {
                    log.warn { "Authenticated user token is missing 'email' claim. Cannot resolve tenant." }
                } else {
                    log.debug { "Attempting to resolve tenant for user email: $userEmail" }
                    val tenantId = businessDataPort.getTenantIdForUser(userEmail)

                    if (tenantId != null) {
                        log.info { "🟢 Tenant resolved for user '$userEmail'. Setting tenant context to: [$tenantId]" }
                        TenantContext.setCurrentTenant(tenantId)
                        tenantIdSet = tenantId
                    } else {
                        log.warn { "🔴 No business tenant found for user email: [$userEmail]. Access denied." }
                        // throwing here stops the request and GlobalExceptionHandler provides response
                        throw TenantNotFoundException(identifierTried = userEmail)
                    }
                }

            } else {
                log.trace { "No authenticated JWT user found, skipping tenant resolution." }
            }
            filterChain.doFilter(request, response)
        } finally {
            if (tenantIdSet != null) {
                log.debug { "🟠 Clearing tenant context (was: $tenantIdSet)" }
                TenantContext.clear()
            }
        }
    }

    // TODO: Define which paths this filter should NOT apply to
    // override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    //     val path = request.servletPath
    //     return path.startsWith("/public/") || path.startsWith("/auth/") //
    // }

}