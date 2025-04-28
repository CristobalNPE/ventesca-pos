package dev.cnpe.ventescaposbe.business.domain.model

import dev.cnpe.ventescaposbe.shared.domain.model.BaseEntity
import dev.cnpe.ventescaposbe.shared.domain.vo.Address
import jakarta.persistence.*

@Entity
@Table(name = "business_branches")
class BusinessBranch(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    var business: Business,

    @Column(name = "branch_name", nullable = false)
    var branchName: String,

    @Embedded
    var address: Address,

    @Column(name = "contact_number")
    var branchContactNumber: String?,

    @Column(name = "is_main_branch", nullable = false)
    var isMainBranch: Boolean = false,

    @Column(name = "branch_manager_id")
    var branchManagerId: String?,

    id: Long? = null,
    version: Int = 0
) : BaseEntity(id, version)