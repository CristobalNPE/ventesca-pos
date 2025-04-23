package dev.cnpe.ventescabekotlin.shared.infrastructure.web.advice

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import dev.cnpe.ventescabekotlin.shared.application.dto.ApiResult
import dev.cnpe.ventescabekotlin.shared.application.dto.ErrorResponse
import dev.cnpe.ventescabekotlin.shared.application.exception.DomainException
import dev.cnpe.ventescabekotlin.shared.application.exception.ErrorCode
import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode.*
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Instant

private val log = KotlinLogging.logger { }

@RestControllerAdvice
class GlobalExceptionHandler(
    private val messageSource: MessageSource
) {

    companion object {
        private const val DEFAULT_ERROR_PREFIX = "error."
        private const val DEFAULT_ERROR_MESSAGE = "Internal Server Error"
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
        handlerMethod: HandlerMethod?
    ): ResponseEntity<ApiResult<Any>> {
        val errorCode = CONSTRAINT_VIOLATION
        val status = errorCode.status

        val fieldErrors = ex.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to getFieldErrorDefaultMessage(fieldError)
        }

        val details = mapOf(
            "fieldErrors" to fieldErrors,
            "totalErrors" to fieldErrors.size
        )
        logClientError(ex, request, handlerMethod, status, errorCode.name, details)

        return buildErrorResponse(request, errorCode, details)
    }

    // Handles validation annotations directly on parameters (e.g., @RequestParam @Size...)
    // Can overlap with MethodArgumentNotValidException but catches slightly different cases
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: ConstraintViolationException,
        request: HttpServletRequest,
        handlerMethod: HandlerMethod?
    ): ResponseEntity<ApiResult<Any>> {
        val errorCode = CONSTRAINT_VIOLATION
        val status = errorCode.status

        val violations = ex.constraintViolations.associate { violation ->
            val propertyPath = violation.propertyPath.toString()
            val field = propertyPath.substringAfterLast(".", propertyPath)
            field to violation.message
        }
        val details = mapOf(
            "fieldErrors" to violations,
            "totalErrors" to violations.size
        )

        logClientError(ex, request, handlerMethod, status, errorCode.name, details)
        return buildErrorResponse(request, errorCode, details)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParams(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest,
        handlerMethod: HandlerMethod?
    ): ResponseEntity<ApiResult<Any>> {
        val errorCode = CONSTRAINT_VIOLATION
        val status = errorCode.status
        val fieldErrors = mapOf(ex.parameterName to "Parameter is required")
        val details = mapOf(
            "fieldErrors" to fieldErrors,
            "totalErrors" to 1
        )
        logClientError(ex, request, handlerMethod, status, errorCode.name, details)
        return buildErrorResponse(request, errorCode, details, ex.parameterName)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
        handlerMethod: HandlerMethod?
    ): ResponseEntity<ApiResult<Any>> {
        val errorCode = DATA_TYPE_MISMATCH
        val status = errorCode.status
        val paramName = ex.name
        val requiredType = ex.requiredType?.simpleName ?: "unknown"
        val invalidValue = ex.value?.toString() ?: "null"

        val details = mutableMapOf<String, Any>(
            "parameterName" to paramName,
            "requiredType" to requiredType,
            "invalidValue" to invalidValue
        )

        addEnumDetailsIfNeeded(details, ex.requiredType)

        logClientError(ex, request, handlerMethod, status, errorCode.name, details)
        return buildErrorResponse(request, errorCode, details, invalidValue, paramName, requiredType)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
        handlerMethod: HandlerMethod?
    ): ResponseEntity<ApiResult<Any>> {
        val status = HttpStatus.BAD_REQUEST

        if (ex.cause is InvalidFormatException) {
            val ife = ex.cause as InvalidFormatException
            val errorCode = DATA_TYPE_MISMATCH
            val fieldPath = ife.path.joinToString(".") { it.fieldName ?: "[index ${it.index}]" }
            val targetType = ife.targetType?.simpleName ?: "unknown"
            val invalidValue = ife.value?.toString() ?: "null"

            val details = mutableMapOf<String, Any>(
                "fieldPath" to fieldPath,
                "requiredType" to targetType,
                "invalidValue" to invalidValue
            )
            addEnumDetailsIfNeeded(details, ife.targetType)

            logClientError(ex, request, handlerMethod, status, errorCode.name, details)
            return buildErrorResponse(request, errorCode, details, invalidValue, fieldPath, targetType)
        } else {
            val errorCode = INVALID_DATA
            logClientError(ex, request, handlerMethod, status, errorCode.name)
            return buildErrorResponse(request, errorCode, null, "Request body is malformed or unreadable")
        }
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(
        ex: DomainException,
        request: HttpServletRequest,
        handlerMethod: HandlerMethod?
    ): ResponseEntity<ApiResult<Any>> {
        val errorCode = ex.errorCode
        val status = errorCode.status
        val details = ex.details

        if (status.is5xxServerError) {
            logServerError(ex, request, handlerMethod, status, errorCode.name)
        } else {
            logClientError(ex, request, handlerMethod, status, errorCode.name, details)
        }

        return buildErrorResponse(request, errorCode, details, *ex.parameters)
    }

    @ExceptionHandler(Exception::class)
    fun handleAnyOtherException(
        ex: Exception,
        request: HttpServletRequest,
        handlerMethod: HandlerMethod?
    ): ResponseEntity<ApiResult<Any>> {
        val errorCode = GENERAL
        val status = errorCode.status

        logServerError(ex, request, handlerMethod, status, errorCode.name)

        return buildErrorResponse(request, errorCode, null)
    }


    /**
     * Adds enum value details to the details map if the target type is an enum.
     */
    private fun addEnumDetailsIfNeeded(details: MutableMap<String, Any>, targetType: Class<*>?) {
        targetType?.takeIf { it.isEnum }?.let { enumClass ->
            details["allowedValues"] = enumClass.enumConstants.map { it.toString() }
        }
    }

    /**
     * Builds the standardized ResponseEntity containing the ApiResult with ErrorResponse.
     */
    private fun buildErrorResponse(
        request: HttpServletRequest,
        errorCode: ErrorCode,
        details: Map<String, Any>?,
        vararg messageParameters: String
    ): ResponseEntity<ApiResult<Any>> {


        val resolvedMessage = resolveMessage(errorCode, *messageParameters)
        val errorResponse = ErrorResponse(
            path = request.requestURI,
            status = errorCode.status.value(),
            code = errorCode.name,
            message = resolvedMessage,
            timestamp = Instant.now(),
            details = details
        )

        val apiResult = ApiResult.error<Any>(errorResponse)
        return ResponseEntity(apiResult, errorCode.status)
    }

    /**
     * Resolves the user-facing error message using MessageSource.
     */
    private fun resolveMessage(errorCode: ErrorCode, vararg parameters: String): String? {
        val messageKey = DEFAULT_ERROR_PREFIX + errorCode.name //error.RESOURCE_NOT_FOUND
        return try {
            messageSource.getMessage(messageKey, parameters.ifEmpty { null }, LocaleContextHolder.getLocale())
        } catch (e: NoSuchMessageException) {
            log.warn { "Missing message key for error code ${errorCode.name} in message source. Key: '$messageKey'" }

            when (errorCode) {
                RESOURCE_NOT_FOUND -> "Resource not found."
                CONSTRAINT_VIOLATION -> "Validation failed."
                DATA_TYPE_MISMATCH -> "Data type mismatch."
                DUPLICATED_RESOURCE -> "Duplicated resource."
                OPERATION_NOT_ALLOWED -> "Operation not allowed."
                INVALID_DATA -> "Invalid data."
                INVALID_STATE -> "Invalid state."
                else -> DEFAULT_ERROR_MESSAGE
            }
        } catch (e: Exception) {
            log.error(e) { "Error resolving message for error code ${errorCode.name}." }
            DEFAULT_ERROR_MESSAGE
        }
    }

    /**
     * Resolves the default message for a FieldError using MessageSource, falling back to the error's default message.
     */
    private fun getFieldErrorDefaultMessage(error: FieldError): String {
        return try {
            messageSource.getMessage(error, LocaleContextHolder.getLocale())
        } catch (e: NoSuchMessageException) {
            //fallback to the msg defined in the validation annotation
            error.defaultMessage ?: "Invalid value."
        } catch (e: Exception) {
            log.error(e) { "Error resolving field error message for field '${error.field}'." }
            error.defaultMessage ?: "Invalid value."
        }
    }
}