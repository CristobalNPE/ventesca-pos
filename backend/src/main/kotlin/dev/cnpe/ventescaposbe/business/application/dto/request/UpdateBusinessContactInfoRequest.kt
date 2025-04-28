package dev.cnpe.ventescaposbe.business.application.dto.request

//TODO: We need to add validation and Schemas to this dto

data class UpdateBusinessContactInfoRequest(
    val phone: String?,
    val email: String?,
    val website: String?
)