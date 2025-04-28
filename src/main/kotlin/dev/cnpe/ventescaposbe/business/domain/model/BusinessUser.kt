package dev.cnpe.ventescaposbe.business.domain.model

import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "business_users",
    indexes = [
        Index(name = "idx_business_user_idp_id", columnList = "idp_user_id", unique = true)
    ]
)
class BusinessUser(

    @Column(name = "idp_user_id", nullable = false, updatable = false, unique = true)
    val idpUserId: String,

    @Column(name = "user_email", nullable = true)
    var userEmail: String?,

    @Column(name = "display_name", nullable = true)
    var displayName: String?,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "business_user_assigned_roles", joinColumns = [JoinColumn(name = "business_user_id")])
    @Column(name = "role_name", nullable = false)
    var roles: MutableSet<String> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    var business: Business?,

    id: Long? = null,
    version: Int = 0
) : BaseEntity(id, version) {

    companion object {

        /**
         * Factory method to create a BusinessUser link instance.
         * Requires the immutable ID from the Identity Provider.
         *
         * @param idpUserId The unique user ID from the IdP ('sub' claim).
         * @param userEmail The user's email (optional, for informational purposes).
         */
        fun createLink(
            idpUserId: String,
            userEmail: String?,
            displayName: String?,
            roles: Set<String>
        ): BusinessUser {
            require(idpUserId.isNotBlank()) { "IdP User ID cannot be blank" }
            return BusinessUser(
                idpUserId = idpUserId,
                userEmail = userEmail,
                displayName = displayName,
                roles = roles.toMutableSet(),
                business = null,
            )
        }
    }
}