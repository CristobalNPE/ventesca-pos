package dev.cnpe.ventescabekotlin.business.domain.model

import dev.cnpe.ventescabekotlin.business.domain.enums.BusinessStatus
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessConfiguration
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessContactInfo
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessDetails
import dev.cnpe.ventescabekotlin.business.domain.vo.BusinessStatusInfo
import dev.cnpe.ventescabekotlin.shared.domain.model.BaseEntity
import dev.cnpe.ventescabekotlin.tenant.vo.TenantIdentifier
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "businesses")
class Business(

    @Column(name = "admin_id", nullable = false, updatable = false)
    val adminId: String,

    @Embedded
    var tenantId: TenantIdentifier,

    @Embedded
    var details: BusinessDetails,

    @Embedded
    var contactInfo: BusinessContactInfo? = null,

    @Embedded
    var configuration: BusinessConfiguration? = null,

    @Embedded
    var statusInfo: BusinessStatusInfo? = null,

    @OneToMany(mappedBy = "business", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val branches: MutableSet<BusinessBranch> = mutableSetOf(),

    @OneToMany(mappedBy = "business", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val users: MutableSet<BusinessUser> = mutableSetOf(),

    id: Long? = null,
    version: Int = 0
) : BaseEntity(id, version) {


    /**
     * Updates the business's status information with the provided status and reason.
     *
     * @param status The new operational status of the business.
     * @param reason An optional reason explaining the context or cause of the status change.
     */
    fun updateStatus(status: BusinessStatus, reason: String? = null) {

        this.statusInfo = BusinessStatusInfo(
            status = status,
            reason = reason,
            changedAt = OffsetDateTime.now()
        )
    }

    // *******************************
    // 🔰 Validation Helpers
    // *******************************

    fun hasValidDetails(): Boolean {
        return details.businessName.isNotBlank()
    }

    fun hasValidContactInfo(): Boolean {
        return contactInfo?.phone?.isNotBlank() ?: false
    }

    fun hasValidConfiguration(): Boolean {
        return configuration?.isValid ?: false
    }

    fun hasMainBranch(): Boolean {
        return branches.any { it.isMainBranch }
    }

    fun getMainBranch(): BusinessBranch? {
        return branches.firstOrNull { it.isMainBranch }
    }

    // *******************************
    // 🔰 Relationship Management
    // *******************************

    fun addBranch(branch: BusinessBranch) {
        branch.business = this
        this.branches.add(branch)
    }

    fun addUser(bUser: BusinessUser) {
        bUser.business = this
        this.users.add(bUser)
    }
}