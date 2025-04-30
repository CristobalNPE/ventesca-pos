package dev.cnpe.ventescaposbe.security.context

/**
 * Interface defining the contract for accessing information about the
 * user associated with the current request context.
 */
interface UserContext {

    val userId: String?
    val email: String?
    val preferredUsername: String?
    val roles: Set<String>
    val tenantId: String?
    val allowedBranchIds: Set<Long>?

}