package dev.cnpe.ventescabekotlin.shared.domain.enums

interface DomainEnum {

    val name: String


}

/**
 * Extension function to generate the standard MessageSource key prefix for any DomainEnum.
 * Example: BusinessStatus.ACTIVE -> "enum.BusinessStatus.ACTIVE"
 */
fun DomainEnum.messageKeyPrefix(): String {
    val className = this::class.simpleName ?: this.javaClass.simpleName
    return "enum.$className.${this.name}"
}
