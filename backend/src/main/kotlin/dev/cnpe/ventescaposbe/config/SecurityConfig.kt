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
                // 🔰 Superuser Admin
                // *******************************
                authorize("/admin/**", hasRole(ROLE_SUPERUSER))

                // *******************************
                // 🔰 Business Admin
                // *******************************
                authorize("/business/**", hasRole(ROLE_BUSINESS_ADMIN))

                // *******************************
                // 🔰 Promotion Management
                // *******************************
                authorize("/admin/promotions/rules/**", hasRole(ROLE_BUSINESS_ADMIN))

                // *******************************
                // 🔰 Brands Management
                // *******************************
                authorize(HttpMethod.GET, "/brands/**", authenticated)
                authorize(HttpMethod.POST, "/brands", hasRole(ROLE_BUSINESS_ADMIN))
                authorize(HttpMethod.PUT, "/brands/**", hasRole(ROLE_BUSINESS_ADMIN))
                authorize(HttpMethod.DELETE, "/brands/**", hasRole(ROLE_BUSINESS_ADMIN))

                // *******************************
                // 🔰 Categories Management
                // *******************************
                authorize(HttpMethod.GET, "/categories/**", authenticated)
                authorize(HttpMethod.POST, "/categories/**", hasRole(ROLE_BUSINESS_ADMIN))
                authorize(HttpMethod.PUT, "/categories/**", hasRole(ROLE_BUSINESS_ADMIN))
                authorize(HttpMethod.DELETE, "/categories/**", hasRole(ROLE_BUSINESS_ADMIN))

                // *******************************
                // 🔰 Suppliers Management
                // *******************************
                authorize(HttpMethod.GET, "/suppliers/**", authenticated)
                authorize(HttpMethod.POST, "/suppliers", hasRole(ROLE_BUSINESS_ADMIN))
                authorize(HttpMethod.PUT, "/suppliers/**", hasRole(ROLE_BUSINESS_ADMIN))
                authorize(HttpMethod.DELETE, "/suppliers/**", hasRole(ROLE_BUSINESS_ADMIN))

                // *******************************
                // 🔰 Catalog Management
                // *******************************
                authorize(HttpMethod.GET, "/products/**", authenticated)
                authorize(HttpMethod.POST, "/products/**", hasRole(ROLE_BUSINESS_ADMIN))
                authorize(HttpMethod.PUT, "/products/**", hasRole(ROLE_BUSINESS_ADMIN))
                // authorize(HttpMethod.DELETE, "/products/**", hasRole(ROLE_BUSINESS_ADMIN)) // TODO

                // *******************************
                // 🔰 Inventory Management
                // *******************************
                authorize(
                    HttpMethod.POST,
                    "/inventory/{productId}/adjustments",
                    hasAnyRole(ROLE_BUSINESS_ADMIN, ROLE_BRANCH_MANAGER)
                )
                authorize(HttpMethod.GET, "/inventory/**", authenticated)
                authorize(HttpMethod.PUT, "/inventory/**", hasAnyRole(ROLE_BUSINESS_ADMIN, ROLE_BRANCH_MANAGER))

                // *******************************
                // 🔰 Order Processing
                // *******************************
                val posOperatorRoles = arrayOf(ROLE_BUSINESS_ADMIN, ROLE_BRANCH_MANAGER, ROLE_SELLER)

                authorize(HttpMethod.POST, "/orders", hasAnyRole(*posOperatorRoles))
                authorize(HttpMethod.POST, "/orders/{orderId}/items", hasAnyRole(*posOperatorRoles))
                authorize(HttpMethod.PUT, "/orders/{orderId}/items/{itemId}", hasAnyRole(*posOperatorRoles))
                authorize(HttpMethod.DELETE, "/orders/{orderId}/items/{itemId}", hasAnyRole(*posOperatorRoles))
                authorize(HttpMethod.POST, "/orders/{orderId}/payments", hasAnyRole(*posOperatorRoles))
                authorize(HttpMethod.POST, "/orders/{orderId}/complete", hasAnyRole(*posOperatorRoles))
                authorize(HttpMethod.POST, "/orders/{orderId}/cancel", hasAnyRole(*posOperatorRoles))

                authorize(HttpMethod.GET, "/orders", authenticated)
                authorize(HttpMethod.GET, "/orders/{orderId}", authenticated)

                authorize(HttpMethod.POST, "/orders/{orderId}/coupon", hasAnyRole(*posOperatorRoles))
                authorize(HttpMethod.POST, "/orders/{orderId}/items/{itemId}/discount", hasAnyRole(*posOperatorRoles))
                authorize(HttpMethod.DELETE, "/orders/{orderId}/items/{itemId}/discount", hasAnyRole(*posOperatorRoles))

                //////////////////////////////////////////////////////
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