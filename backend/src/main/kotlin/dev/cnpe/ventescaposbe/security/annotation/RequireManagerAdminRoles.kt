package dev.cnpe.ventescaposbe.security.annotation

import dev.cnpe.ventescaposbe.config.SecurityConfig
import org.springframework.security.access.prepost.PreAuthorize


/**
 * Annotation that enforces access control, requiring the user to have either
 * a Branch Manager role or a Business Admin role.
 *
 * Can be applied to classes or functions to restrict access exclusively
 * to users who are granted one of these specific roles.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize(
    "hasAnyRole(" +
            "${SecurityConfig.ROLE_BRANCH_MANAGER}, " +
            "${SecurityConfig.ROLE_BUSINESS_ADMIN})"
)
annotation class RequireManagerAdminRoles()
