package dev.cnpe.ventescaposbe.reporting.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

@Schema(description = "Common request parameters for generating reports.")
data class ReportRequestParams(

    @field:NotNull(message = "Start date cannot be null.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(
        description = "Start date/time for the report period (inclusive, ISO 8601 format).",
        required = true,
        example = "2024-10-26T00:00:00Z"
    )
    val startDate: OffsetDateTime,

    @field:NotNull(message = "End date cannot be null.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(
        description = "End date/time for the report period (exclusive, ISO 8601 format).",
        required = true,
        example = "2024-10-27T00:00:00Z"
    )
    val endDate: OffsetDateTime,

    @Schema(description = "Optional ID to filter report by a specific branch.", example = "101")
    @field:Positive(message = "Branch ID must be positive if provided.")
    val branchId: Long? = null,

    @Schema(description = "Optional User IdP ID to filter report by a specific cashier.", example = "auth0|abc123xyz")
    val userIdpId: String? = null,

    @Schema(description = "Optional limit for reports returning top N results (e.g., top 5 products).", example = "10")
    @field:Positive(message = "Limit must be positive if provided.")
    val limit: Int? = 5
) {
    init {
        require(!endDate.isBefore(startDate)) { "End date cannot be before start date." }
    }
}