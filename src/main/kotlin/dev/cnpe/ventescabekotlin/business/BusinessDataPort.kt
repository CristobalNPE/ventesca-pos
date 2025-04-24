package dev.cnpe.ventescabekotlin.business

interface BusinessDataPort {

    fun getTenantIdForUser(userEmail: String): String?

}