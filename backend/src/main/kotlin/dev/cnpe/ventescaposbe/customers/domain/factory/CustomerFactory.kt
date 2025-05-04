package dev.cnpe.ventescaposbe.customers.domain.factory

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.customers.domain.model.Customer
import dev.cnpe.ventescaposbe.shared.domain.vo.Address
import dev.cnpe.ventescaposbe.shared.domain.vo.PersonalInfo
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CustomerFactory {

    fun create(
        firstName: String,
        lastName: String?,
        taxId: String?,
        email: String?,
        phone: String?,
        address: Address?,
        notes: String?,
        defaultCurrency: String
    ): Customer {
        require(firstName.isNotBlank()) { "First name cannot be blank" }

        val formattedFirstName = firstName.trim().replaceFirstChar { it.titlecase() }
        val formattedLastName = lastName?.trim()?.takeIf { it.isNotEmpty() }?.replaceFirstChar { it.titlecase() }


        val personalInfo = PersonalInfo(
            firstName = formattedFirstName,
            lastName = formattedLastName,
            personalId = taxId?.trim()?.takeIf { it.isNotEmpty() }
        )

        val zeroMoney = Money(amount = BigDecimal.ZERO, currencyCode = defaultCurrency)

        return Customer(
            personalInfo = personalInfo,
            email = email?.trim()?.takeIf { it.isNotEmpty() },
            phone = phone?.trim()?.takeIf { it.isNotEmpty() },
            address = address,
            totalSpent = zeroMoney,
            notes = notes?.trim()?.takeIf { it.isNotEmpty() }
        )
    }

}