package dev.cnpe.ventescaposbe.security.adapters

import dev.cnpe.ventescaposbe.security.config.KeycloakAdminProperties
import dev.cnpe.ventescaposbe.security.exception.IdpAccessException
import dev.cnpe.ventescaposbe.security.exception.IdpRoleAssignmentException
import dev.cnpe.ventescaposbe.security.exception.IdpUserCreationException
import dev.cnpe.ventescaposbe.security.exception.IdpUserNotFoundException
import dev.cnpe.ventescaposbe.security.ports.IdentityProviderPort
import dev.cnpe.ventescaposbe.security.ports.dto.NewUserData
import dev.cnpe.ventescaposbe.security.ports.dto.UserIdentity
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.ws.rs.core.Response
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

private const val TARGET_REALM = "ventesca"

@Component
@Profile("keycloak")
class KeycloakIdentityProviderAdapter(
    private val properties: KeycloakAdminProperties
) : IdentityProviderPort {

    private val keycloakAdminClient: Keycloak by lazy { buildKeycloakAdminClient() }

    private fun buildKeycloakAdminClient(): Keycloak {
        log.info { "Building Keycloak Admin Client for server: ${properties.serverUrl}, realm: ${properties.realm}, clientId: ${properties.clientId}" }

        return KeycloakBuilder.builder()
            .serverUrl(properties.serverUrl)
            .realm(properties.realm)
            .grantType(properties.grantType)
            .clientId(properties.clientId)
            .clientSecret(properties.clientSecret)
            .username(properties.username)
            .password(properties.password)
            .build()
    }

    private fun getTargetRealm(): RealmResource {
        return keycloakAdminClient.realm(TARGET_REALM)
    }

    private fun usersResource(): UsersResource = getTargetRealm().users()

    override fun createUser(
        userData: NewUserData,
        roles: Set<String>,
        attributes: Map<String, String>
    ): UserIdentity {
        log.debug { "Attempting to create user in Keycloak: ${userData.email}" }

        val userRep = UserRepresentation().apply {
            username = userData.username ?: userData.email
            email = userData.email
            firstName = userData.firstName
            lastName = userData.lastName
            isEnabled = true
            isEmailVerified = false // TODO CHECK
            this.attributes = attributes.mapValues { listOf(it.value) }

            userData.initialPassword?.let { pwd ->
                val credential = CredentialRepresentation().apply {
                    type = CredentialRepresentation.PASSWORD
                    value = pwd
                    isTemporary = true
                }
                credentials = listOf(credential)
            }
        }

        try {
            val response: Response = usersResource().create(userRep)

            when (response.status) {
                Response.Status.CREATED.statusCode -> {
                    val location = response.location
                    val userId = location.path.substringAfterLast("/")
                    log.info { "Successfully created user in Keycloak: ${userData.email}, ID: $userId" }

                    if (roles.isNotEmpty()) {
                        try {
                            assignRolesToUserInternal(userId, roles)
                        } catch (roleEx: Exception) {
                            log.error(roleEx) { "User $userId created, but failed to assign roles: $roles" }
                            throw IdpRoleAssignmentException("Failed to assign roles after user creation", roleEx)
                        }
                    }

                    //fetch created user to return full UserIdentity
                    val createdUserRep = usersResource().get(userId)?.toRepresentation()
                    return UserIdentity(
                        id = userId,
                        email = createdUserRep?.email ?: userData.email,
                        username = createdUserRep?.username,
                        firstName = createdUserRep?.firstName,
                        lastName = createdUserRep?.lastName
                    )
                }

                Response.Status.CONFLICT.statusCode -> {
                    log.warn { "User creation conflict for ${userData.email}. User likely already exists." }
                    throw IdpUserCreationException(
                        userData.email,
                        "User creation conflict (email/username likely exists)."
                    )
                }

                else -> {
                    val errorMsg = response.readEntity(String::class.java) ?: "Unknown Keycloak error"
                    log.error { "Keycloak user creation failed for ${userData.email}. Status: ${response.status}, Reason: $errorMsg" }
                    throw IdpUserCreationException(userData.email, "Keycloak API error: ${response.status} - $errorMsg")
                }
            }
        } catch (e: Exception) {
            log.error(e) { "Exception during Keycloak user creation for ${userData.email}" }
            throw IdpAccessException("Failed to create user due to communication error: ${e.message}", e)
        }
    }

    override fun findUserByEmail(email: String): UserIdentity? {
        log.debug { "Searching for user in Keycloak by email: $email" }

        try {
            val userReps: List<UserRepresentation> = usersResource().searchByEmail(email, true)
            return userReps.firstOrNull()?.let {
                log.info { "Found user by email $email. ID: ${it.id}" }
                UserIdentity(
                    id = it.id,
                    email = it.email,
                    username = it.username,
                    firstName = it.firstName,
                    lastName = it.lastName
                )
            } ?: run {
                log.info { "No user found with email: $email" }
                null
            }
        } catch (e: Exception) {
            log.error(e) { "Exception during Keycloak user search for email $email" }
            throw IdpAccessException("Failed to search user by email due to communication error: ${e.message}", e)
        }
    }

    override fun assignRolesToUser(userId: String, roles: Set<String>) {
        log.debug { "Assigning roles $roles to Keycloak user ID: $userId" }
        if (roles.isEmpty()) {
            log.warn { "No roles provided to assign to user $userId." }
            return
        }
        assignRolesToUserInternal(userId, roles)
    }

    override fun findUserById(userId: String): UserIdentity? {
        log.debug { "Finding user in Keycloak by ID: $userId" }
        return try {
            usersResource().get(userId)?.toRepresentation()?.let {
                UserIdentity(
                    id = it.id,
                    email = it.email,
                    username = it.username,
                    firstName = it.firstName,
                    lastName = it.lastName
                )
            }
        } catch (nf: jakarta.ws.rs.NotFoundException) {
            log.info { "User not found in Keycloak with ID: $userId" }
            null
        } catch (e: Exception) {
            log.error(e) { "Exception finding Keycloak user $userId" }
            throw IdpAccessException("Failed to find user $userId: ${e.message}", e)
        }
    }

    override fun updateUserAttributes(
        userId: String,
        attributes: Map<String, String>
    ) {
        log.debug { "Updating attributes for Keycloak user ID: $userId with $attributes" }
        try {
            val userResource = usersResource().get(userId) ?: throw IdpUserNotFoundException(userId)
            val userRep = userResource.toRepresentation()

            val keycloakAttributes = attributes.mapValues { listOf(it.value) }

            val newAttributes = (userRep.attributes ?: emptyMap()).toMutableMap()
            newAttributes.putAll(keycloakAttributes)
            userRep.attributes = newAttributes

            userResource.update(userRep)
            log.info { "Successfully updated attributes for user $userId" }

        } catch (nf: jakarta.ws.rs.NotFoundException) {
            log.error(nf) { "User $userId not found during attribute update." }
            throw IdpUserNotFoundException(userId)
        } catch (e: Exception) {
            log.error(e) { "Exception updating attributes for user $userId" }
            throw IdpAccessException("Failed update attributes for $userId: ${e.message}", e)
        }
    }

    override fun deleteUser(userId: String) {
        log.warn { "Attempting to delete Keycloak user ID: $userId" }
        try {
            val response = usersResource().delete(userId)
            if (response.statusInfo.family == Response.Status.Family.SUCCESSFUL) {
                log.info { "Successfully deleted Keycloak user ID: $userId" }
            } else if (response.status == Response.Status.NOT_FOUND.statusCode) {
                log.error { "User $userId not found during deletion attempt." }
                throw IdpUserNotFoundException(userId)
            } else {
                val errorMsg = response.readEntity(String::class.java) ?: "Unknown Keycloak error"
                log.error { "Keycloak user deletion failed for $userId. Status: ${response.status}, Reason: $errorMsg" }
                throw IdpAccessException("Keycloak API error during delete: ${response.status} - $errorMsg")
            }
        } catch (nf: jakarta.ws.rs.NotFoundException) {
            log.error(nf) { "User $userId not found during deletion." }
            throw IdpUserNotFoundException(userId)
        } catch (e: Exception) {
            log.error(e) { "Exception during Keycloak user deletion for $userId" }
            throw IdpAccessException("Failed to delete user $userId: ${e.message}", e)
        }
    }

    override fun getUserAttributes(userId: String): Map<String, List<String>> {
        log.debug { "Getting attributes for Keycloak user ID: $userId" }
        try {
            return usersResource().get(userId)?.toRepresentation()?.attributes ?: emptyMap()
        } catch (nf: jakarta.ws.rs.NotFoundException) {
            log.error(nf) { "User $userId not found when getting attributes." }
            throw IdpUserNotFoundException(userId)
        } catch (e: Exception) {
            log.error(e) { "Exception getting attributes for user $userId" }
            throw IdpAccessException("Failed get attributes for $userId: ${e.message}", e)
        }
    }


    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun assignRolesToUserInternal(userId: String, roles: Set<String>) {

        try {
            val userResource = usersResource().get(userId) ?: throw IdpUserNotFoundException(userId)

            val availableRoles = getTargetRealm().roles().list()?.associateBy { it.name } ?: emptyMap()
            val rolesToAssign = roles
                .mapNotNull { roleName ->
                    availableRoles[roleName] ?: run {
                        log.warn { "Role '$roleName' not found in realm '$TARGET_REALM'. Skipping assignment." }
                        null
                    }
                }
                .toList()

            if (rolesToAssign.isNotEmpty()) {
                log.info { "Adding realm roles ${rolesToAssign.map { it.name }} to user $userId" }
                userResource.roles().realmLevel().add(rolesToAssign)
                // TODO: if need to REPLACE roles, first get existing, remove, then add.
            } else {
                log.warn { "None of the requested roles $roles exist in realm $TARGET_REALM for user $userId." }
            }
        } catch (nf: jakarta.ws.rs.NotFoundException) {
            log.error(nf) { "User $userId not found in Keycloak during role assignment." }
            throw IdpUserNotFoundException(userId)
        } catch (e: Exception) {
            log.error(e) { "Exception during Keycloak role assignment for user $userId" }
            throw IdpAccessException("Failed to assign roles to user $userId: ${e.message}", e)
        }
    }
}