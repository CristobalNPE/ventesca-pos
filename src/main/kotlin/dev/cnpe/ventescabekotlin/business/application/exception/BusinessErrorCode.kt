package dev.cnpe.ventescabekotlin.business.application.exception

import dev.cnpe.ventescabekotlin.shared.application.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CONFLICT

enum class BusinessErrorCode(override val status: HttpStatus) : ErrorCode {
    ACTIVATION_FAILED(CONFLICT),
    USER_ALREADY_LINKED(CONFLICT)
}