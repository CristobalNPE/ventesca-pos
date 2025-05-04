package dev.cnpe.ventescaposbe.sessions.application.api

import dev.cnpe.ventescaposbe.sessions.application.api.dto.SessionBasicInfo

/**
 * Port defining operations for retrieving basic Session information needed by other modules.
 */
interface SessionInfoPort {

    /**
     * Finds the currently open session for a specific user at a specific branch.
     *
     * @param userIdpId The ID of the user (cashier).
     * @param branchId The ID of the branch.
     * @return SessionBasicInfo if an open session exists, null otherwise.
     */
    fun findOpenSession(userIdpId: String, branchId: Long): SessionBasicInfo?

    /**
     * Checks if there is an open session for a specific user at a specific branch.
     * More lightweight than findOpenSession if only existence check is needed.
     *
     * @param userIdpId The ID of the user (cashier).
     * @param branchId The ID of the branch.
     * @return True if an open session exists, false otherwise.
     */
    fun isSessionOpen(userIdpId: String, branchId: Long): Boolean
}