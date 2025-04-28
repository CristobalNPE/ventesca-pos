package dev.cnpe.ventescaposbe.tenant.exception

import dev.cnpe.ventescaposbe.shared.application.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

enum class TenantErrorCode(override val status: HttpStatus = INTERNAL_SERVER_ERROR) : ErrorCode {

    TENANT_RESOLUTION_FAILED(FORBIDDEN), // User authenticated but no tenant found/accessible (403)
    TENANT_SETUP_FAILED(INTERNAL_SERVER_ERROR)
}
