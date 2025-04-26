package dev.cnpe.ventescabekotlin.suppliers.domain

import dev.cnpe.ventescabekotlin.shared.domain.model.BaseEntity
import dev.cnpe.ventescabekotlin.shared.domain.vo.Address
import dev.cnpe.ventescabekotlin.shared.domain.vo.ContactInfo
import dev.cnpe.ventescabekotlin.shared.domain.vo.PersonalInfo
import jakarta.persistence.*

@Entity
@Table(name = "suppliers")
class Supplier(

    @Column(name = "business_name", nullable = false)
    var name: String, // This is the name of the supplier's business, not to be confused with the business module

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "firstName", column = Column(name = "representative_name")),
        AttributeOverride(name = "lastName", column = Column(name = "representative_last_name")),
        AttributeOverride(name = "personalId", column = Column(name = "representative_personal_id"))
    )
    var representativeInfo: PersonalInfo = PersonalInfo.empty(),

    @Embedded
    var contactInfo: ContactInfo = ContactInfo.empty(),

    @Embedded
    var address: Address = Address.empty(),

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,

    id: Long? = null,
    version: Int = 0
) : BaseEntity(id, version)