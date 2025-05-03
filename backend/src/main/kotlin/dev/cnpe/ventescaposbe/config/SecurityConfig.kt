package dev.cnpe.ventescaposbe.config

import dev.cnpe.ventescaposbe.security.TenantAuthenticationFilter
import dev.cnpe.ventescaposbe.security.config.JwtAuthConverter
import dev.cnpe.ventescaposbe.security.filters.UserContextPopulationFilter
import dev.cnpe.ventescaposbe.shared.infrastructure.web.filters.MDCLoggingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val tenantAuthenticationFilter: TenantAuthenticationFilter,
    private val userContextPopulationFilter: UserContextPopulationFilter,
    private val mdcLoggingFilter: MDCLoggingFilter,
    private val jwtAuthConverter: JwtAuthConverter
) {

    companion object {
        const val ROLE_SUPERUSER = "SUPERUSER"
        const val ROLE_BUSINESS_ADMIN = "BUSINESS_ADMIN"
        const val ROLE_BRANCH_MANAGER = "BRANCH_MANAGER"
        const val ROLE_SELLER = "SELLER"
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {

                // *******************************
                // 🔰 Public Endpoints
                // *******************************
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/favicon.ico", permitAll)
                authorize("/dev/enums/**", permitAll)

                // *******************************
                // 🔰 Other Requests - Require Authentication
                // *******************************
                authorize(anyRequest, authenticated)
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            cors { disable() } //TODO: configure
            csrf { disable() }
            oauth2ResourceServer {
                jwt {
                    jwtAuthenticationConverter = jwtAuthConverter
                }
            }
            addFilterAfter<BearerTokenAuthenticationFilter>(tenantAuthenticationFilter)
            addFilterAfter<TenantAuthenticationFilter>(userContextPopulationFilter)
            addFilterAfter<UserContextPopulationFilter>(mdcLoggingFilter)
        }
        return http.build()
    }
}