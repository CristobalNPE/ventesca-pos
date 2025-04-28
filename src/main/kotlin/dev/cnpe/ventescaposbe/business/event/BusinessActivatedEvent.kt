package dev.cnpe.ventescaposbe.business.event

data class BusinessActivatedEvent(
    val businessId: Long,
    val businessName: String
)