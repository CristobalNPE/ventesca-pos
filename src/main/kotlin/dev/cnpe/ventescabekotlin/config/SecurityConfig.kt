package dev.cnpe.ventescabekotlin.config

import dev.cnpe.ventescabekotlin.security.TenantAuthenticationFilter
import dev.cnpe.ventescabekotlin.security.filters.UserContextPopulationFilter
import dev.cnpe.ventescabekotlin.shared.infrastructure.web.filters.MDCLoggingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val tenantAuthenticationFilter: TenantAuthenticationFilter,
    private val userContextPopulationFilter: UserContextPopulationFilter,
    private val mdcLoggingFilter: MDCLoggingFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            cors { disable() }
            csrf { disable() }
            oauth2ResourceServer { jwt { } }
            addFilterAfter<BearerTokenAuthenticationFilter>(tenantAuthenticationFilter)
            addFilterAfter<TenantAuthenticationFilter>(userContextPopulationFilter)
            addFilterAfter<UserContextPopulationFilter>(mdcLoggingFilter)
        }
        return http.build()
    }
}