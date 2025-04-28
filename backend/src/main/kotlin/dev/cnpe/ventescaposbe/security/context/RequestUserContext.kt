package dev.cnpe.ventescaposbe.security.context

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class RequestUserContext(
    override var userId: String? = null,
    override var email: String? = null,
    override var preferredUsername: String? = null,
    override var roles: Set<String> = emptySet(),
    override var tenantId: String? = null
) : UserContext {
}