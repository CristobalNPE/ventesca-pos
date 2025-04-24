package dev.cnpe.ventescabekotlin.business.domain.model

import dev.cnpe.ventescabekotlin.shared.domain.model.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "business_users",
    indexes = [
        Index(name = "idx_business_user_email", columnList = "user_email")
    ]
)
class BusinessUser(

    @Column(name = "user_email", nullable = false, updatable = false)
    val userEmail: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    var business: Business?,

    id: Long? = null,
    version: Int = 0
) : BaseEntity(id, version) {

    companion object {

        /**
         * Factory method to create a BusinessUser instance from an email.
         */
        fun fromEmail(userEmail: String): BusinessUser {
            require(userEmail.isNotBlank()) { "User email cannot be blank" }
            return BusinessUser(
                userEmail = userEmail,
                business = null,
            )
        }
    }

}