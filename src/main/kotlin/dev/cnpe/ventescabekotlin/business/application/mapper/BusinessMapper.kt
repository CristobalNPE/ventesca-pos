package dev.cnpe.ventescabekotlin.business.application.mapper

import dev.cnpe.ventescabekotlin.business.application.dto.response.BusinessBranchInfo
import dev.cnpe.ventescabekotlin.business.application.dto.response.BusinessDetailedResponse
import dev.cnpe.ventescabekotlin.business.domain.model.Business
import dev.cnpe.ventescabekotlin.business.domain.model.BusinessBranch
import org.springframework.stereotype.Component

@Component
class BusinessMapper {

    /**
     * Maps a Business entity to its detailed DTO representation.
     */
    fun toDetailedDto(business: Business): BusinessDetailedResponse {
        val branchInfos = business.branches.map { branch ->
            mapBranchToInfoDto(branch)
        }.toSet()

        return BusinessDetailedResponse(
            id = business.id!!,
            details = business.details,
            contactInfo = business.contactInfo,
            configuration = business.configuration,
            status = business.statusInfo
                ?: throw IllegalStateException("Business ${business.id} has null statusInfo"), // Status should exist
            branches = branchInfos
        )
    }


    /**
     * Helper to map a BusinessBranch entity to a BusinessBranchInfo DTO.
     */
    fun mapBranchToInfoDto(branch: BusinessBranch): BusinessBranchInfo {
        return BusinessBranchInfo(
            branchName = branch.branchName,
            address = branch.address
                ?: TODO("Handle null address in branch mapping if possible"), // Handle null address
            contactNumber = branch.branchContactNumber,
            isMainBranch = branch.isMainBranch
            // Add manager info if needed in DTO
        )
    }

}