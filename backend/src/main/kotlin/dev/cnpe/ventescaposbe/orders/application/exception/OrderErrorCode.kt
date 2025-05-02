package dev.cnpe.ventescaposbe.orders.application.exception

import dev.cnpe.ventescaposbe.shared.application.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class OrderErrorCode(override val status: HttpStatus) : ErrorCode {
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST)
}