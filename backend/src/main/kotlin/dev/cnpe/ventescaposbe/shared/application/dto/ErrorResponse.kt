package dev.cnpe.ventescaposbe.shared.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response structure.")
data class ErrorResponse(

    @Schema(description = "The request path that resulted in the error.", example = "/brands/123", required = false)
    val path: String?,

    @Schema(description = "The HTTP status code.", example = "404", required = true)
    val status: Int,

    @Schema(description = "Application-specific error code.", example = "RESOURCE_NOT_FOUND", required = true)
    val code: String,

    @Schema(
        description = "A human-readable explanation specific to this occurrence of the problem.",
        example = "Brand not found for ID: 123",
        required = false
    )
    val message: String?,

    @Schema(description = "Timestamp indicating when the error occurred.", required = true)
    val timestamp: Instant,


//    @Schema(
//        description = """
//            A specific reason code providing more context for the error.
//            The actual type depends on the context of the error.
//            """,
//        required = false,
//        implementation = OperationNotAllowedReason::class
//        // If implementation doesn't work, I would have to manually add every class involved. Not good:
////                oneOf = [
////            CategoryOperationNotAllowedReason::class, // Reference specific enum schemas
////            // ProductOperationNotAllowedReason::class // Add other reason enums here
////        ]
//    )
//    val reason: String? = null,

    @Schema(
        description = "An optional map containing additional error details (e.g., validation errors).",
        required = false
    )
    val details: Map<String, Any>? = null
)
