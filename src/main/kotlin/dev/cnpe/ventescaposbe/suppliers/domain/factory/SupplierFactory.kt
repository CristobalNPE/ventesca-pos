package dev.cnpe.ventescaposbe.suppliers.domain.factory

import dev.cnpe.ventescaposbe.suppliers.domain.Supplier
import org.springframework.stereotype.Component

@Component
class SupplierFactory(

) {

    /**
     * Creates a basic, active Supplier instance with empty contact/personal/address info.
     * The business name is sanitized (capitalized, trimmed).
     *
     * @param name The legal business name of the supplier.
     * @return A new Supplier instance.
     */
    fun create(name: String): Supplier {
        require(name.isNotBlank()) { "Supplier business name cannot be blank" }

        val sanitizedName = name.trim().replaceFirstChar { it.uppercase() }
        return Supplier(
            name = sanitizedName,
        )
    }

}