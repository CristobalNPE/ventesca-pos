package dev.cnpe.ventescaposbe.shared.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import dev.cnpe.ventescaposbe.shared.application.dto.ApiResult.Companion.error
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Standard API response wrapper. Can represent either a successful result with data
 * or an error result with error details.
 *
 * @param T The type of data included in a successful response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper for success or error outcomes.")
class ApiResult<T> private constructor(

    @Schema(description = "Indicates if the request was successful.", required = true, example = "false")
    val success: Boolean,

    @Schema(
        description = "The response data payload if the request was successful (absent on error).",
        required = false
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val data: T?,

    @Schema(description = "The error details if the request failed (absent on success).", required = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val error: ErrorResponse?

) {
    companion object {

        /**
         * Creates a successful ApiResult instance.
         * @param data The data payload for the successful response.
         */
        fun <T> success(data: T): ApiResult<T> {
            return ApiResult(success = true, data = data, error = null)
        }

        /**
         * Creates an error ApiResult instance using a pre-built ErrorResponse.
         * @param error The detailed ErrorResponse object.
         */
        fun <T> error(error: ErrorResponse): ApiResult<T> {
            return ApiResult(success = false, data = null, error = error)
        }
    }

}