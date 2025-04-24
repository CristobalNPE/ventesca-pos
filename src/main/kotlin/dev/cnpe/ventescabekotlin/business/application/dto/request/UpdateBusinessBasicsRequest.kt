package dev.cnpe.ventescabekotlin.business.application.dto.request

//TODO: We need to add validation and Schemas to this dto
data class UpdateBusinessBasicsRequest(
    val businessName: String?,
    val brandMessage: String?
)

