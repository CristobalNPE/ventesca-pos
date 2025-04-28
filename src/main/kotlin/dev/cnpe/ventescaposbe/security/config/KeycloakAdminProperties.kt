package dev.cnpe.ventescaposbe.security.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "keycloak.admin")
@Validated
data class KeycloakAdminProperties(

    @field:NotBlank
    val serverUrl: String, // http://localhost:8180

    @field:NotBlank
    val realm: String,

    @field:NotBlank
    val clientId: String,

    val clientSecret: String? = null,
    val username: String? = null,
    val password: String? = null,

    val grantType: String = "client_credentials"

) {
    init {
        when (grantType) {
            "client_credentials" -> {
                require(!clientSecret.isNullOrBlank()) { "keycloak.admin.client-secret must be set for client_credentials grant" }
            }
            "password" -> {
                require(!username.isNullOrBlank()) { "keycloak.admin.username must be set for password grant" }
                require(!password.isNullOrBlank()) { "keycloak.admin.password must be set for password grant" }
            }
            else -> {
                // other grant types later
            }
        }
    }
}
