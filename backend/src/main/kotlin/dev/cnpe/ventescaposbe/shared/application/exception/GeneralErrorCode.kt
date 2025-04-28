package dev.cnpe.ventescaposbe.shared.application.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*

enum class GeneralErrorCode(override val status: HttpStatus = INTERNAL_SERVER_ERROR) : ErrorCode {

    GENERAL,
    CONSTRAINT_VIOLATION(BAD_REQUEST),
    DATA_TYPE_MISMATCH(BAD_REQUEST),
    RESOURCE_NOT_FOUND(NOT_FOUND),
    DUPLICATED_RESOURCE(CONFLICT),
    OPERATION_NOT_ALLOWED(METHOD_NOT_ALLOWED),
    INVALID_DATA(BAD_REQUEST),
    INVALID_STATE(BAD_REQUEST),
    AUTHENTICATION_ERROR(UNAUTHORIZED),
    INSUFFICIENT_CONTEXT(INTERNAL_SERVER_ERROR)
}