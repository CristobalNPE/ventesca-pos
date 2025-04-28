package dev.cnpe.ventescaposbe.business.application.exception

import dev.cnpe.ventescaposbe.shared.application.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CONFLICT

enum class BusinessErrorCode(override val status: HttpStatus) : ErrorCode {
    ACTIVATION_FAILED(CONFLICT),
    USER_ALREADY_LINKED(CONFLICT)
}