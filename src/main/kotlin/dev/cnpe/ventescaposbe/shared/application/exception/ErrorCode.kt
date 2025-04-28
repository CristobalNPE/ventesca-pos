package dev.cnpe.ventescaposbe.shared.application.exception

import org.springframework.http.HttpStatus

/**
 * Interface defining the contract for application error codes.
 *
 */
interface ErrorCode {

    /**
     * The HTTP status associated with this error code.
     */
    val status: HttpStatus

    /**
     * The unique name/identifier of the error code (typically the enum constant name)
     */
    val name: String

}