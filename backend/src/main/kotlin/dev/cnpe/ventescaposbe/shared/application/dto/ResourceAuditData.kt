package dev.cnpe.ventescaposbe.shared.application.dto

import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "Audit information for a resource.")
data class ResourceAuditData(

    @Schema(description = "User who created the resource.")
    val createdBy: String,

    @Schema(description = "User who last modified the resource.")
    val lastModifiedBy: String?,

    @Schema(description = "Timestamp of creation.", format = "date-time")
    val createdAt: OffsetDateTime,

    @Schema(description = "Timestamp of last modification.", format = "date-time")
    val lastModifiedAt: OffsetDateTime

) {
    companion object {

        fun fromBaseEntity(entity: BaseEntity): ResourceAuditData {
            return ResourceAuditData(
                createdBy = entity.createdBy,
                lastModifiedBy = entity.lastModifiedBy,
                createdAt = entity.createdAt,
                lastModifiedAt = entity.lastModifiedAt
            )
        }
    }
}

