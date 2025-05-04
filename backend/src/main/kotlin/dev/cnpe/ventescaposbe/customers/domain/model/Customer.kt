package dev.cnpe.ventescaposbe.customers.domain.model

import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import dev.cnpe.ventescaposbe.shared.domain.vo.Address
import dev.cnpe.ventescaposbe.shared.domain.vo.PersonalInfo
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "customers",
    indexes = [
        Index(name = "idx_customer_email", columnList = "email"),
        Index(name = "idx_customer_phone", columnList = "phone"),
        Index(name = "idx_customer_tax_id", columnList = "customer_tax_id", unique = true)
    ]
)
class Customer(

    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "firstName",
            column = Column(name = "customer_first_name", nullable = false, length = 50)
        ),
        AttributeOverride(name = "lastName", column = Column(name = "customer_last_name", length = 50)),
        AttributeOverride(name = "personalId", column = Column(name = "customer_tax_id", length = 50, unique = true))
    )
    var personalInfo: PersonalInfo,

    @Column(name = "email", length = 100, unique = true)
    var email: String? = null,

    @Column(name = "phone", length = 25)
    var phone: String? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street", column = Column(name = "address_street", length = 100)),
        AttributeOverride(name = "city", column = Column(name = "address_city", length = 50)),
        AttributeOverride(name = "country", column = Column(name = "address_country", length = 50)),
        AttributeOverride(name = "postalCode", column = Column(name = "address_postal_code", length = 20))
    )
    var address: Address? = Address.empty(),

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_spent_amount", nullable = false)),
        AttributeOverride(
            name = "currencyCode",
            column = Column(name = "total_spent_currency", nullable = false, length = 3)
        )
    )
    var totalSpent: Money,

    @Column(name = "total_orders", nullable = false)
    var totalOrders: Int = 0,

    @Column(name = "last_order_date")
    var lastOrderDate: OffsetDateTime? = null,

    @Column(name = "notes", length = 500)
    var notes: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    id: Long? = null,
    version: Int = 0

) : BaseEntity(id, version) {

    fun getFullName(): String {
        return if (personalInfo.lastName != null) {
            "${personalInfo.firstName} ${personalInfo.lastName}"
        } else {
            personalInfo.firstName
        }
    }
}