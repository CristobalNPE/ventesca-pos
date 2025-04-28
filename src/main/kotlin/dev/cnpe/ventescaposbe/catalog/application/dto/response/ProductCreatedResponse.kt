package dev.cnpe.ventescaposbe.catalog.application.dto.response

import dev.cnpe.ventescaposbe.catalog.domain.enums.ProductStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Response after successfully creating a product.")
data class ProductCreatedResponse(

    @Schema(description = "ID of the newly created product.", requiredMode = Schema.RequiredMode.REQUIRED)
    val id: Long,

    @Schema(description = "Initial status of the created product.", requiredMode = Schema.RequiredMode.REQUIRED)
    val status: ProductStatus,

    @Schema(description = "Timestamp of creation.", requiredMode = Schema.RequiredMode.REQUIRED, format = "date-time")
    val createdAt: OffsetDateTime
)
