package dev.cnpe.ventescabekotlin.business.event

data class BusinessActivatedEvent(
    val businessId: Long,
    val businessName: String
)