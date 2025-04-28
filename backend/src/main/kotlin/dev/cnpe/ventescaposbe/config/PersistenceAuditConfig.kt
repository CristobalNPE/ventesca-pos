package dev.cnpe.ventescaposbe.config

import dev.cnpe.ventescaposbe.security.context.UserContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.OffsetDateTime
import java.util.*

private val log = KotlinLogging.logger {}

@Configuration
@EnableJpaAuditing(
    auditorAwareRef = "auditorProvider",
    dateTimeProviderRef = "auditingDateTimeProvider"
)
class PersistenceAuditConfig {

    companion object {
        const val SYSTEM_AUDITOR = "VENTESCA_SYS" // System user for non-request contexts
    }

    /**
     * Provides the identifier of the current user for auditing purposes.
     * Attempts to retrieve from UserContext (request scope), falls back to SecurityContextHolder,
     * and defaults to SYSTEM_AUDITOR if no authenticated user is found.
     */
    @Bean("auditorProvider")
    fun auditorProvider(
        userContextProvider: ObjectProvider<UserContext>
    ): AuditorAware<String> {
        return AuditorAware<String> {
            val requestScopeAuditor = try {
                userContextProvider.ifAvailable?.preferredUsername
            } catch (e: Exception) {
                log.trace(e) { "Could not get UserContext (likely no active request scope)."}
                null
            }


            val auditor = requestScopeAuditor
                ?: getAuditorFromSecurityContext()
                ?: SYSTEM_AUDITOR

            log.trace { "Auditor resolved to: $auditor" }
            Optional.of(auditor)
        }
    }

    /**
     * Fallback function to get auditor from SecurityContextHolder.
     * Needed for contexts where request-scoped UserContext might not be available.
     */
    private fun getAuditorFromSecurityContext(): String? {
        return SecurityContextHolder.getContext().authentication?.let { auth ->
            if (auth.isAuthenticated && auth !is AnonymousAuthenticationToken) {
                (auth as? JwtAuthenticationToken)?.token?.getClaimAsString("preferred_username")
                    ?: auth.name
            } else null
        }
    }

    /**
     * Provides the current date and time for auditing purposes.
     * Ensures OffsetDateTime is used consistently.
     */
    @Bean("auditingDateTimeProvider")
    fun dateTimeProvider(): DateTimeProvider {
        return DateTimeProvider { Optional.of(OffsetDateTime.now()) }
    }


}