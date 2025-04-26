package dev.cnpe.ventescabekotlin.shared.application.exception

import dev.cnpe.ventescabekotlin.shared.application.exception.GeneralErrorCode.*

/**
 * Creates a standard DomainException for resource not found errors.
 *
 * @param entityType User-friendly name of the entity type (e.g., "Brand", "Category").
 * @param id The ID of the resource that was not found.
 * @return DomainException instance with RESOURCE_NOT_FOUND code and details.
 */
fun createResourceNotFoundException(entityType: String, id: Any): DomainException {
    return DomainException(
        errorCode = RESOURCE_NOT_FOUND,
        details = mapOf(
            "entityType" to entityType,
            "${entityType.lowercase()}Id" to id
        ),
        parameters = arrayOf(entityType, id.toString())
    )
}


/**
 * Creates a standard DomainException for duplicated resource errors.
 *
 * @param field The name of the field that has a duplicate value.
 * @param value The value that caused the duplication conflict.
 * @return DomainException instance with DUPLICATED_RESOURCE code and details.
 */
fun createDuplicatedResourceException(field: String, value: Any): DomainException {
    val valueString = value.toString()
    return DomainException(
        errorCode = DUPLICATED_RESOURCE,
        details = mapOf(
            "field" to field,
            "value" to value
        ),
        parameters = arrayOf(field, valueString)
    )
}

/**
 * Creates a standard DomainException for operation not allowed errors with a specific reason.
 *
 * @param reason The specific reason enum constant (implementing OperationNotAllowedReason).
 * @param entityId Optional ID of the entity involved.
 * @param additionalDetails Optional map for context-specific details beyond the reason.
 * @return DomainException instance with OPERATION_NOT_ALLOWED code and details.
 */
fun createOperationNotAllowedException(
    reason: OperationNotAllowedReason,
    entityId: Any? = null,
    additionalDetails: Map<String, Any>? = null
): DomainException {
    val detailsMap = mutableMapOf<String, Any>("reason" to reason.name)
    entityId?.let { detailsMap["entityId"] = it }
    additionalDetails?.let { detailsMap.putAll(it) }

    return DomainException(
        errorCode = OPERATION_NOT_ALLOWED,
        details = detailsMap.toMap()
    )
}