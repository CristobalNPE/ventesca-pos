package dev.cnpe.ventescaposbe.security.ports

import dev.cnpe.ventescaposbe.security.exception.IdpAccessException
import dev.cnpe.ventescaposbe.security.exception.IdpUserCreationException
import dev.cnpe.ventescaposbe.security.exception.IdpUserNotFoundException
import dev.cnpe.ventescaposbe.security.ports.dto.NewUserData
import dev.cnpe.ventescaposbe.security.ports.dto.UserIdentity

/**
 * Port defining interactions with the external Identity Provider (IdP)
 * for user creation and role management required by application services.
 * Implementations will adapt to specific IdPs like Keycloak or Supabase.
 */
interface IdentityProviderPort {


    /**
     * Creates a new user in the IdP.
     * Assigns specified application roles and potentially links them to metadata.
     *
     * @param userData Details of the user to create (email, names, optional password).
     * @param roles Set of application role *names* (e.g., "BUSINESS_ADMIN") to assign.
     * @param attributes Optional map of custom attributes/metadata to store with the user (e.g., "tenant_id").
     * @return UserIdentity containing the unique ID assigned by the IdP and other core details.
     * @throws IdpUserCreationException if user creation fails (e.g., duplicate email).
     * @throws IdpAccessException for general communication errors with the IdP.
     */
    @Throws(IdpUserCreationException::class, IdpAccessException::class)
    fun createUser(
        userData: NewUserData,
        roles: Set<String>,
        attributes: Map<String, String> = emptyMap()
    ): UserIdentity

    /**
     * Finds a user in the IdP by their email address.
     * Useful for checking if a user already exists before attempting creation.
     *
     * @param email The email address to search for.
     * @return UserIdentity if a user with that email is found, null otherwise.
     * @throws IdpAccessException for general communication errors with the IdP.
     */
    @Throws(IdpAccessException::class)
    fun findUserByEmail(email: String): UserIdentity?

    /**
     * Assigns a set of application roles to an existing user in the IdP.
     * Implementations should handle whether this replaces or adds to existing roles.
     *
     * @param userId The unique ID of the user in the IdP (e.g., 'sub' claim).
     * @param roles The set of application role *names* to assign.
     * @throws IdpUserNotFoundException if the user ID doesn't exist in the IdP.
     * @throws IdpAccessException for general communication errors or role assignment failures.
     */
    @Throws(IdpUserNotFoundException::class, IdpAccessException::class)
    fun assignRolesToUser(userId: String, roles: Set<String>)

    /** Finds a user by their unique IdP ID. */
    @Throws(IdpAccessException::class)
    fun findUserById(userId: String): UserIdentity?

    /** Updates attributes for an existing user (e.g., name, custom attributes). */
    @Throws(IdpUserNotFoundException::class, IdpAccessException::class)
    fun updateUserAttributes(userId: String, attributes: Map<String, String>) // Attributes to update/add

    /** Deletes a user from the IdP. Use with caution! */
    @Throws(IdpUserNotFoundException::class, IdpAccessException::class)
    fun deleteUser(userId: String)

    /** Gets the custom attributes for a user (needed to check tenant_id). */
    @Throws(IdpUserNotFoundException::class, IdpAccessException::class)
    fun getUserAttributes(userId: String): Map<String, List<String>> // Keycloak stores attributes as List<String>

}