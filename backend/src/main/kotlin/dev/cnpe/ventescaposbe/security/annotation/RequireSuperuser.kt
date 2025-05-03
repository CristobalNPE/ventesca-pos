package dev.cnpe.ventescaposbe.security.annotation

import dev.cnpe.ventescaposbe.config.SecurityConfig
import org.springframework.security.access.prepost.PreAuthorize


/**
 * Annotation that enforces access control, requiring the user to have a Superuser role.
 *
 * Can be applied to classes or methods to restrict access exclusively
 * to users who have been granted the specific Superuser role.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('${SecurityConfig.ROLE_SUPERUSER}')")
annotation class RequireSuperuser()
