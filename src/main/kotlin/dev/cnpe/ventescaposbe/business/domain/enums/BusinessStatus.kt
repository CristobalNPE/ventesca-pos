package dev.cnpe.ventescaposbe.business.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum

/**
 * Represents the operational status of a business.
 * User-facing names and descriptions are resolved via MessageSource
 * using keys like "enum.BusinessStatus.ACTIVE.name" and "enum.BusinessStatus.ACTIVE.description".
 */
enum class BusinessStatus: DomainEnum {

    NON_CREATED,
    PENDING,
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    TERMINATED;

}