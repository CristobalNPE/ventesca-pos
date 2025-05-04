package dev.cnpe.ventescaposbe.customers.application.mapper

import dev.cnpe.ventescaposbe.customers.application.api.dto.CustomerBasicInfo
import dev.cnpe.ventescaposbe.customers.application.dto.response.CustomerDetailedResponse
import dev.cnpe.ventescaposbe.customers.application.dto.response.CustomerSummaryResponse
import dev.cnpe.ventescaposbe.customers.domain.model.Customer
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import org.springframework.stereotype.Component

@Component
class CustomerMapper {

    fun toSummary(customer: Customer): CustomerSummaryResponse {
        return CustomerSummaryResponse(
            id = customer.id!!,
            fullName = customer.getFullName(),
            email = customer.email,
            phone = customer.phone,
            taxId = customer.personalInfo.personalId,
            isActive = customer.isActive
        )
    }

    fun toDetailed(customer: Customer): CustomerDetailedResponse {
        return CustomerDetailedResponse(
            id = customer.id!!,
            firstName = customer.personalInfo.firstName,
            lastName = customer.personalInfo.lastName,
            taxId = customer.personalInfo.personalId,
            email = customer.email,
            phone = customer.phone,
            address = customer.address,
            totalSpent = customer.totalSpent,
            totalOrders = customer.totalOrders,
            lastOrderDate = customer.lastOrderDate,
            notes = customer.notes,
            isActive = customer.isActive,
            auditData = ResourceAuditData.fromBaseEntity(customer)
        )
    }

    fun toBasicInfo(customer: Customer): CustomerBasicInfo {
        return CustomerBasicInfo(
            id = customer.id!!,
            fullName = customer.getFullName(),
            email = customer.email,
            phone = customer.phone
        )
    }
}