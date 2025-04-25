package dev.cnpe.ventescabekotlin.business.application.dto.response

import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessConfiguration
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessContactInfo
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessDetails
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessStatusInfo
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Represents detailed information about a business.")
data class BusinessDetailedResponse(

    @Schema(description = "Unique identifier for the business.")
    val id: Long,

    @Schema(description = "Core identifying details.")
    val details: BusinessDetails,

    @Schema(description = "Main contact information.")
    val contactInfo: BusinessContactInfo?,

    @Schema(description = "Business configuration settings.")
    val configuration: BusinessConfiguration?,

    @Schema(description = "Current operational status of the business.")
    val status: BusinessStatusInfo?,

    @Schema(description = "List of branches associated with the business.")
    val branches: Set<BusinessBranchInfo>
)
