package dev.cnpe.ventescabekotlin.business.application.dto.request

//TODO: We need to add validation and Schemas to this dto
data class CreateBusinessBranchRequest(
    val managerId: String?,
    val branchName: String?,
    val addressStreet: String?,
    val addressCity: String?,
    val addressZipCode: String?,
    val addressCountry: String?,
    val contactNumber: String?
)