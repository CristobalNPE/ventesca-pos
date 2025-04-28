package dev.cnpe.ventescaposbe.security.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Custom converter to extract roles from Keycloak JWT claims (realm_access.roles)
 * and map them to Spring Security GrantedAuthorities with the 'ROLE_' prefix.
 * Also includes standard scope-based authorities.
 */
@Component
class JwtAuthConverter(
    //TODO: Inject properties if claim names or prefix need to be configurable
    // private val keycloakProperties: KeycloakClaimProperties
) : Converter<Jwt, AbstractAuthenticationToken> {

    private val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    private val principalClaimName = "preferred_username"
    private val resourceAccessClaim = "realm_access"
    private val rolesClaim = "roles"
    private val rolePrefix = "ROLE_"


    override fun convert(jwt: Jwt): AbstractAuthenticationToken? {

        val scopeAuthorities = jwtGrantedAuthoritiesConverter.convert(jwt) ?: emptySet()

        val realmRoles = extractRealmRoles(jwt)

        val allAuthorities = Stream.concat(scopeAuthorities.stream(), realmRoles.stream())
            .collect(Collectors.toSet())

        val principalName = jwt.getClaimAsString(principalClaimName) ?: jwt.subject

        return JwtAuthenticationToken(jwt, allAuthorities, principalName)
    }

    /**
     * Extracts roles from the 'realm_access.roles' claim and prefixes them.
     */
    private fun extractRealmRoles(jwt: Jwt): Collection<GrantedAuthority> {
        val realmAccess = jwt.getClaimAsMap(resourceAccessClaim)
        val roles = realmAccess?.get(rolesClaim) as? Collection<*>

        return roles?.mapNotNull { role ->
            (role as? String)?.takeIf { it.isNotBlank() }?.let { roleName ->
                SimpleGrantedAuthority(rolePrefix + roleName.uppercase())
            }
        }?.toSet() ?: emptySet()
    }

    // FIXME: Extracting Resource Access (Client) Roles
    // private fun extractResourceRoles(jwt: Jwt, resourceId: String): Collection<GrantedAuthority> {
    //     val resourceAccess = jwt.getClaimAsMap("resource_access")
    //     val clientAccess = resourceAccess?.get(resourceId) as? Map<*, *>
    //     val clientRoles = clientAccess?.get("roles") as? Collection<*>
    //     return clientRoles?.mapNotNull { role -> /* ... map and prefix ... */ }?.toSet() ?: emptySet()
    // }


}