package dev.cnpe.ventescaposbe.suppliers.application.dto.response

import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import dev.cnpe.ventescaposbe.shared.domain.vo.Address
import dev.cnpe.ventescaposbe.shared.domain.vo.ContactInfo
import dev.cnpe.ventescaposbe.shared.domain.vo.PersonalInfo
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Complete detailed information about a supplier.")
data class SupplierDetailedResponse(

    @Schema(
        description = "Unique identifier of the supplier.",
        example = "55",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val id: Long,

    @Schema(
        description = "The legal business name of the supplier.",
        example = "Global Tech Supplies",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val name: String,

    @Schema(
        description = "Details of the supplier representative.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val representative: PersonalInfo,

    @Schema(
        description = "Contact details for the supplier.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val contactInfo: ContactInfo,

    @Schema(
        description = "Primary address of the supplier.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val address: Address,

    @Schema(
        description = "Indicates if the supplier is currently active.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isActive: Boolean,

    @Schema(description = "Audit information.", requiredMode = Schema.RequiredMode.REQUIRED)
    val auditData: ResourceAuditData
)
