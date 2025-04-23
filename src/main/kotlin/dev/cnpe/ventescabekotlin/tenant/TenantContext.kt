package dev.cnpe.ventescabekotlin.tenant

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger { }

/**
 * Holds the identifier for the current tenant within the current thread.
 * Uses an InheritableThreadLocal so child threads can access the tenant context.
 */
object TenantContext {

    private val CURRENT_TENANT = InheritableThreadLocal<String?>()


    /**
     * Returns the tenant identifier for the current thread, or null if none is set.
     */
    fun getCurrentTenant(): String? {
        return CURRENT_TENANT.get()
    }

    /**
     * Sets the tenant identifier for the current thread.
     * Setting null effectively clears it, but using clear() is more explicit.
     *
     * @param tenant The tenant identifier string, or null to clear.
     */
    fun setCurrentTenant(tenant: String?) {
        if (tenant != null) {
            log.trace { "Setting tenant context to : $tenant" }
            CURRENT_TENANT.set(tenant)
        } else {
            clear()
        }
    }

    /**
     * Clears the tenant identifier for the current thread.
     * Should be called at the end of a request or operation.
     */
    fun clear() {
        log.trace { "Clearing tenant context (was: ${CURRENT_TENANT.get()})" }
        CURRENT_TENANT.remove()
    }
}