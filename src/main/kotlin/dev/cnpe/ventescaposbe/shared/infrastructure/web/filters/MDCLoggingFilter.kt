package dev.cnpe.ventescaposbe.shared.infrastructure.web.filters

import dev.cnpe.ventescaposbe.security.context.UserContext
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.*

private val log = KotlinLogging.logger {}

@Component
class MDCLoggingFilter(
    private val userContext: UserContext
) : OncePerRequestFilter() {

    companion object {
        private const val REQUEST_ID = "requestId"
        private const val USER_ID = "userId"
        private const val TENANT_ID = "tenantId"
        private const val REQUEST_URI = "requestUri"
        private const val REQUEST_METHOD = "requestMethod"
        private const val MAX_UA_LENGTH = 60
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = UUID.randomUUID().toString().substring(0, 8)
        val startTime = System.currentTimeMillis()

        try {
            MDC.put(REQUEST_ID, requestId)
            MDC.put(REQUEST_URI, request.requestURI)
            MDC.put(REQUEST_METHOD, request.method)

            response.setHeader("X-Request-Id", requestId)

            userContext.userId?.let { MDC.put(USER_ID, it) }
            userContext.tenantId?.let { MDC.put(TENANT_ID, it) }

            if (log.isDebugEnabled()) {
                log.debug {
                    """
                    |📥 Request Started:
                    |   URI     : ${request.method} ${request.requestURI}
                    |   Trace ID: $requestId
                    |   Client  : ${getClientInfo(request)}
                    """.trimMargin()
                }
            }

            filterChain.doFilter(request, response)

        } finally {
            val duration = System.currentTimeMillis() - startTime
            val status = response.status
            val emoji = getStatusEmoji(status)

            log.info {
                """
                |${emoji} Request Completed:
                |   URI     : ${request.method} ${request.requestURI}
                |   Status  : $status
                |   Time    : ${duration}ms
                |   Trace ID: $requestId
                """.trimMargin()
            }
            MDC.clear()
        }
    }

    private fun getStatusEmoji(status: Int): String = when {
        status < 300 -> "✅" // 2xx Success
        status < 400 -> "↪️" // 3xx Redirect
        status < 500 -> "⚠️" // 4xx Client Error
        else -> "🔥" // 5xx Server Error
    }

    private fun getClientInfo(request: HttpServletRequest): String {
        val userAgent = request.getHeader("User-Agent")
        val clientIp = getClientIp(request)
        return "$clientIp (UA: ${abbreviateUserAgent(userAgent)})"
    }

    private fun getClientIp(request: HttpServletRequest): String {
        return request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
            ?: request.remoteAddr
    }

    private fun abbreviateUserAgent(userAgent: String?): String {
        return userAgent?.take(MAX_UA_LENGTH)?.let { if (userAgent.length > MAX_UA_LENGTH) "$it..." else it }
            ?: "Unknown"
    }
}