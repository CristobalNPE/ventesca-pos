package dev.cnpe.ventescabekotlin.shared.infrastructure.web.advice

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.method.HandlerMethod
import java.util.stream.Collectors

private val log = KotlinLogging.logger { }

/**
 * Logs client-side errors (4xx) with structured context.
 */
internal fun logClientError(
    exception: Exception,
    request: HttpServletRequest,
    handlerMethod: HandlerMethod?,
    status: HttpStatus,
    errorCode: String,
    details: Map<String, Any>? = null
) {
    val controllerName = handlerMethod?.beanType?.simpleName ?: "UnknownController"
    val methodName = handlerMethod?.method?.name ?: "unknownMethod"
    val requestDetails = formatRequestDetails(request)
    val userContext = getUserContext()
    val detailString = formatDetails(details)

    log.warn {
        """
        |🟠 API Warning [${status.value()} ${status.reasonPhrase}]:
        |   Error Code: $errorCode
        |   Controller: $controllerName.$methodName
        |   Request   : $requestDetails
        |   User      : $userContext
        |   Exception : ${exception::class.simpleName} - ${exception.message ?: "No specific message."}
        |   Details   : $detailString
        """.trimMargin()
    }
}

/**
 * Logs server-side errors (5xx) with structured context and stack trace excerpt.
 */
internal fun logServerError(
    exception: Exception,
    request: HttpServletRequest,
    handlerMethod: HandlerMethod?,
    status: HttpStatus,
    errorCode: String
) {
    val controllerName = handlerMethod?.beanType?.simpleName ?: "UnknownController"
    val methodName = handlerMethod?.method?.name ?: "unknownMethod"
    val requestDetails = formatRequestDetails(request)
    val userContext = getUserContext()

    log.error(exception) {
        """
        |🔴 API Error [${status.value()} ${status.reasonPhrase}]:
        |   Error Code: $errorCode
        |   Controller: $controllerName.$methodName
        |   Request   : $requestDetails
        |   User      : $userContext
        |   Exception : ${exception::class.simpleName} - ${exception.message ?: "Internal server error."}
        """.trimMargin()
    }
}

private fun formatRequestDetails(request: HttpServletRequest): String {
    val params = request.parameterMap?.entries?.stream()
        ?.map { "${it.key}: ${it.value.joinToString()}" }
        ?.collect(Collectors.joining(", ")) ?: ""

    val queryString = if (params.isNotEmpty()) "?$params" else ""

    return "${request.method} ${request.requestURI}$queryString (Client: ${getClientIp(request)}"
}

private fun getClientIp(request: HttpServletRequest): String {
    return request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
        ?: request.remoteAddr
}

private fun getUserContext(): String {
    // I'm not sure about this, since I don't know how UserContextUtils will look like in the future when we implement it. Maybe it will look similar to this file?

//    val email = UserContextUtils.getCurrentUserEmail()
//    val userId = UserContextUtils.getCurrentUserId() // Assuming this might return null if not authenticated
//
//    return when {
//        email != null && userId != null -> "$email (ID: $userId)"
//        email != null -> email
//        userId != null -> "User ID: $userId"
//        else -> "Anonymous / Unauthenticated"
//    }

    return "😢 Not implemented"
}

private fun formatDetails(details: Map<String, Any>?): String {
    if (details.isNullOrEmpty()) {
        return "None"
    }
    return details.entries.joinToString(separator = "\n|     - ", prefix = "\n|     - ") {
        "${it.key}: ${it.value}"
    }
}


