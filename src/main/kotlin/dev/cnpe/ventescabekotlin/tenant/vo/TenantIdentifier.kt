package dev.cnpe.ventescabekotlin.tenant.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.*

/**
 * Represents a type-safe identifier for a tenant.
 *
 * @property value The underlying string representation of the tenant ID.
 */
@Embeddable
data class TenantIdentifier(
    @Column(name = "tenant_id", nullable = false, updatable = false, unique = true)
    val value: String
) {
    init {
        require(value.isNotBlank()) { "TenantIdentifier value cannot be blank" }
    }

    companion object {
        private const val PREFIX_MAX_LENGTH = 10
        private const val SUFFIX_LENGTH = 8


        /**
         * Generates a new TenantIdentifier based on a business name.
         * Creates a cleaned prefix from the name and appends a random suffix.
         * Example: "My Awesome Corp" -> "myawesomec_a1b2c3d4"
         *
         * @param businessName The name to base the identifier on.
         * @return A newly generated TenantIdentifier.
         */
        fun generateFrom(businessName: String): TenantIdentifier {
            require(businessName.isNotBlank()) { "Business name cannot be blank for generating Tenant ID" }

            val suffix = UUID.randomUUID().toString().replace("-", "").take(SUFFIX_LENGTH)
            val cleanedPrefix = businessName.lowercase()
                .replace(Regex("[^a-z0-9]"), "")
                .take(PREFIX_MAX_LENGTH)

            val finalPrefix = cleanedPrefix.ifEmpty { "tenant" }

            val generatedValue = "${finalPrefix}_$suffix"
            return TenantIdentifier(generatedValue)
        }
    }
}
