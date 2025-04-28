package dev.cnpe.ventescaposbe.shared.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@Schema(description = "Generic paginated response wrapper.")
data class PageResponse<T>(

    @get:Schema(description = "The actual data content for the current page.")
    val content: List<T>,

    @get:Schema(description = "Metadata about the pagination.", required = true)
    val pagination: PaginationMetadata
) {
    companion object {
        /**
         * Factory method to create a PageResponse from a Spring Data Page object.
         */
        fun <T> from(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                pagination = PaginationMetadata(
                    pageNumber = page.number,
                    pageSize = page.size,
                    totalElements = page.totalElements,
                    totalPages = page.totalPages,
                    first = page.isFirst,
                    last = page.isLast
                )
            )
        }
    }

    @Schema(description = "Details about the pagination state.")
    data class PaginationMetadata(
        @Schema(description = "Current page number (0-indexed).", example = "0")
        val pageNumber: Int,
        @Schema(description = "Number of items per page.", example = "20")
        val pageSize: Int,
        @Schema(description = "Total number of items across all pages.", example = "150")
        val totalElements: Long,
        @Schema(description = "Total number of pages.", example = "8")
        val totalPages: Int,
        @Schema(description = "Indicates if this is the first page.")
        val first: Boolean,
        @Schema(description = "Indicates if this is the last page.")
        val last: Boolean
    )
}
