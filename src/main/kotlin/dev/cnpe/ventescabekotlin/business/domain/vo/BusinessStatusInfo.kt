package dev.cnpe.ventescabekotlin.business.domain.vo

import dev.cnpe.ventescabekotlin.business.domain.enums.BusinessStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

@Schema(description = "Information about the business's current status.")
@Embeddable
data class BusinessStatusInfo(

    @Schema(
        description = "Current operational status of the business.",
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @field:NotNull
    val status: BusinessStatus,

    @Schema(
        description = "Reason for the current status (if applicable).",
        example = "Initial setup complete",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Column(name = "status_reason")
    @field:Size(max = 255)
    val reason: String?,

    @Schema(description = "Timestamp when the status last changed.", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "status_changed_at", nullable = false)
    @field:NotNull
    val changedAt: OffsetDateTime
)
