package dev.cnpe.ventescaposbe.config

import dev.cnpe.ventescaposbe.security.TenantAuthenticationFilter
import dev.cnpe.ventescaposbe.security.config.JwtAuthConverter
import dev.cnpe.ventescaposbe.security.filters.UserContextPopulationFilter
import dev.cnpe.ventescaposbe.shared.infrastructure.web.filters.MDCLoggingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize("/favicon.ico", permitAll)
                authorize("/api/enums/**", permitAll)

                authorize("/admin/**", hasRole("SUPERUSER"))

                authorize("/business/**", hasRole("BUSINESS_ADMIN"))

                authorize(HttpMethod.GET, "/brands/**", authenticated)
                authorize(HttpMethod.GET, "/categories/**", authenticated)
                authorize(HttpMethod.GET, "/suppliers/**", authenticated)
                authorize(HttpMethod.GET, "/products/**", authenticated)
                authorize(HttpMethod.GET, "/inventory/**", authenticated)

                authorize(HttpMethod.POST, "/brands", hasRole("BUSINESS_ADMIN"))
                authorize(HttpMethod.POST, "/categories/**", hasRole("BUSINESS_ADMIN"))
                authorize(HttpMethod.POST, "/suppliers", hasRole("BUSINESS_ADMIN"))
                authorize(HttpMethod.POST, "/products/**", hasRole("BUSINESS_ADMIN"))

                authorize(HttpMethod.PUT, "/brands/**", hasRole("BUSINESS_ADMIN"))
                authorize(HttpMethod.PUT, "/categories/**", hasRole("BUSINESS_ADMIN"))
                authorize(HttpMethod.PUT, "/suppliers/**", hasRole("BUSINESS_ADMIN"))
                authorize(HttpMethod.PUT, "/products/**", hasRole("BUSINESS_ADMIN"))
                authorize(HttpMethod.PUT, "/inventory/**", hasAnyRole("BUSINESS_ADMIN", "BRANCH_MANAGER"))

                authorize(HttpMethod.DELETE, "/brands/**", hasRole("BUSINESS_ADMIN"))
                authorize(HttpMethod.DELETE, "/categories/**", hasRole("BUSINESS_ADMIN"))
                authorize(HttpMethod.DELETE, "/suppliers/**", hasRole("BUSINESS_ADMIN"))
                // authorize(HttpMethod.DELETE, "/products/**", hasRole("BUSINESS_ADMIN"))

                authorize(HttpMethod.POST, "/orders/**", hasAnyRole("BUSINESS_ADMIN", "BRANCH_MANAGER", "SELLER"))

                authorize(anyRequest, authenticated)
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            cors { disable() }
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