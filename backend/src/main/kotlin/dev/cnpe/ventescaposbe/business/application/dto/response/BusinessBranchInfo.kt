package dev.cnpe.ventescaposbe.business.application.dto.response

import dev.cnpe.ventescaposbe.shared.domain.vo.Address
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Information about a specific business branch.")
data class BusinessBranchInfo(

    @Schema(
        description = "Unique identifier for the branch.",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val branchId: Long,

    @Schema(
        description = "Name of the branch.",
        example = "Downtown Office",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val branchName: String,

    @Schema(description = "Address of the branch.", requiredMode = Schema.RequiredMode.REQUIRED)
    val address: Address,

    @Schema(
        description = "Primary contact number for the branch.",
        example = "+1-555-123-4567",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    val contactNumber: String?,

    @Schema(
        description = "Indicates if this is the main/headquarters branch.",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val isMainBranch: Boolean

    // TODO: Add manager info if needed
)