package dev.cnpe.ventescabekotlin.shared.domain.model

import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(
    @Id
    @SequenceGenerator(
        name = "primary_sequence",
        sequenceName = "primary_sequence",
        allocationSize = 1,
        initialValue = 10000
    )
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "primary_sequence"
    )
    @Column(name = "id")
    open var id: Long? = null,

    @Version
    @Column(name = "version", nullable = false)
    open var version: Int = 0

) {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: OffsetDateTime
        protected set

    @LastModifiedDate
    @Column(name = "last_modified_at", nullable = false)
    lateinit var lastModifiedAt: OffsetDateTime
        protected set

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    lateinit var createdBy: String
        protected set

    @LastModifiedBy
    @Column(name = "last_modified_by")
    var lastModifiedBy: String? = null
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        if (this::class.java != other::class.java) {
            val otherActualClass =
                if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
            if (this.javaClass != otherActualClass) return false
        }

        if (other !is BaseEntity) return false

        return this.id != null && this.id == other.id
    }

    override fun hashCode(): Int {
        return if (id != null) {
            id.hashCode()
        } else {
            System.identityHashCode(this)
        }
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}(id=$id)"
    }
}