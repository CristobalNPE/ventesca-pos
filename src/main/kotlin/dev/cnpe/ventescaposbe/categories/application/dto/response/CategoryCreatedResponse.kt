package dev.cnpe.ventescaposbe.categories.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Response after successfully creating a category.")
data class CategoryCreatedResponse(

    @Schema(
        description = "ID of the newly created category.",
        example = "123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val id: Long,

    @Schema(
        description = "Name of the created category.",
        example = "Electronics",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val name: String,

    @Schema(
        description = "Generated code for the created category.",
        example = "CAT-ELEC",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val code: String,

    @Schema(
        description = "Timestamp when the category was created.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val createdAt: OffsetDateTime

)
