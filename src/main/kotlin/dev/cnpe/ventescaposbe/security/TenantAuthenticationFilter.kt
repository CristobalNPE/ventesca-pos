package dev.cnpe.ventescaposbe.security

import dev.cnpe.ventescaposbe.business.application.api.BusinessDataPort
import dev.cnpe.ventescaposbe.tenant.TenantContext
import dev.cnpe.ventescaposbe.tenant.exception.TenantNotFoundException
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

    companion object {
        private val EXCLUDED_PATHS = listOf(
            "/admin/",
            "/dev/",
            "/v3/api-docs/",
            "/swagger-ui"
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var tenantIdSet: String? = null

        try {
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication != null && authentication.isAuthenticated && authentication is JwtAuthenticationToken) {
                val idpUserId = authentication.token.subject

                if (idpUserId.isNullOrBlank()) {
                    log.warn { "Authenticated user token is missing 'sub' claim. Cannot resolve tenant." }
                    throw TenantNotFoundException(identifierTried = "sub_claim_missing")
                } else {
                    log.debug { "Attempting to resolve tenant for user IdP ID: $idpUserId for path ${request.servletPath}" }
                    val tenantId = businessDataPort.getTenantIdForUser(idpUserId)

                    if (tenantId != null) {
                        log.info { "🟢 Tenant resolved for user IdP ID '$idpUserId'. Setting tenant context to: [$tenantId]" }
                        TenantContext.setCurrentTenant(tenantId)
                        tenantIdSet = tenantId
                    } else {
                        log.warn { "🔴 No business tenant found for user IdP ID: [$idpUserId] for path ${request.servletPath}. Access denied." }
                        throw TenantNotFoundException(identifierTried = idpUserId)
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

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {

        val path = request.servletPath
        return EXCLUDED_PATHS.any { path.startsWith(it) }
    }
}