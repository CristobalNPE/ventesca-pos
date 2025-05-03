package dev.cnpe.ventescaposbe.security.annotation

import dev.cnpe.ventescaposbe.config.SecurityConfig
import org.springframework.security.access.prepost.PreAuthorize

/**
* Meta-annotation to require the user to have one of the roles
* considered a Point-of-Sale Operator (Seller, Branch Manager, Business Admin).
* To be used on methods or classes requiring these permissions.
*/
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@PreAuthorize(
    "hasAnyRole(" +
            "${SecurityConfig.ROLE_SELLER}, " +
            "${SecurityConfig.ROLE_BRANCH_MANAGER}, " +
            "${SecurityConfig.ROLE_BUSINESS_ADMIN})"
)
annotation class RequirePosOperatorRoles()
