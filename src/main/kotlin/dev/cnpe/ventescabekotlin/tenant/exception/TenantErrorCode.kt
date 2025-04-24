package dev.cnpe.ventescabekotlin.tenant.exception

import dev.cnpe.ventescabekotlin.shared.application.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*

enum class TenantErrorCode(override val status: HttpStatus = INTERNAL_SERVER_ERROR) : ErrorCode {

    TENANT_RESOLUTION_FAILED(FORBIDDEN), // User authenticated but no tenant found/accessible (403)
    TENANT_SETUP_FAILED(INTERNAL_SERVER_ERROR)
}
