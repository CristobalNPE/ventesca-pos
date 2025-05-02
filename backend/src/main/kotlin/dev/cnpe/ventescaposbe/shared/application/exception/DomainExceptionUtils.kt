package dev.cnpe.ventescaposbe.shared.application.exception

import dev.cnpe.ventescaposbe.shared.application.exception.GeneralErrorCode.*

/**
 * Creates a standard DomainException for resource not found errors.
 * Uses the RESOURCE_NOT_FOUND error code and provides structured details.
 * The user-facing message is resolved via MessageSource using "error.RESOURCE_NOT_FOUND" key
 * and the provided parameters.
 *
 * @param entityType User-friendly name of the entity type (e.g., "Brand", "Category"). Used as the first message parameter.
 * @param id The ID or identifier of the resource that was not found. Used as the second message parameter.
 * @return DomainException instance.
 */
fun createResourceNotFoundException(entityType: String, id: Any): DomainException {
    return DomainException(
        errorCode = RESOURCE_NOT_FOUND,
        details = mapOf(
            "entityType" to entityType,
            "identifier" to id.toString()
        ),
        parameters = arrayOf(entityType, id.toString()),
        message = "Resource Not Found: $entityType with id $id"
    )
}


/**
 * Creates a standard DomainException for duplicated resource errors.
 * Uses the DUPLICATED_RESOURCE error code and provides structured details.
 * The user-facing message is resolved via MessageSource using "error.DUPLICATED_RESOURCE" key
 * and the provided parameters.
 *
 * @param field The name of the field that has a duplicate value (e.g., "name", "email"). Used as the first message parameter.
 * @param value The value that caused the duplication conflict. Used as the second message parameter.
 * @return DomainException instance.
 */
fun createDuplicatedResourceException(field: String, value: Any): DomainException {
    val valueString = value.toString()
    return DomainException(
        errorCode = DUPLICATED_RESOURCE,
        details = mapOf(
            "field" to field,
            "value" to valueString
        ),
        parameters = arrayOf(field, valueString),
        message = "Duplicate Resource: Field '$field' with value '$valueString'"
    )
}

/**
 * Creates a standard DomainException for operation not allowed errors with a specific reason.
 * Uses the OPERATION_NOT_ALLOWED error code and provides structured details including the reason.
 * The user-facing message can be resolved via MessageSource using a key like
 * "error.OPERATION_NOT_ALLOWED.<ReasonName>" or a generic "error.OPERATION_NOT_ALLOWED"
 * with parameters.
 *
 * @param reason The specific reason enum constant (implementing OperationNotAllowedReason).
 * @param entityId Optional ID or identifier of the entity involved.
 * @param additionalDetails Optional map for context-specific details beyond the reason.
 * @param parameters Vararg String parameters to be used for MessageSource formatting.
 * @return DomainException instance.
 */
fun createOperationNotAllowedException(
    reason: OperationNotAllowedReason,
    entityId: Any? = null,
    additionalDetails: Map<String, Any>? = null,
    vararg parameters: String,
): DomainException {
    val detailsMap = mutableMapOf<String, Any>("reason" to reason.name)
    entityId?.let { detailsMap["entityId"] = it }
    additionalDetails?.let { detailsMap.putAll(it) }

    return DomainException(
        errorCode = OPERATION_NOT_ALLOWED,
        details = detailsMap.toMap(),
        parameters = parameters,
        message = "Operation Not Allowed: Reason ${reason.name}" + (entityId?.let { " on entity $it" } ?: "")
    )
}

/**
 * Creates a standard DomainException for invalid state errors.
 * Uses the INVALID_STATE error code and provides structured details including an optional reason.
 * The user-facing message is resolved via MessageSource using "error.INVALID_STATE" key
 * potentially with parameters if provided.
 *
 * @param reason A specific string describing the invalid state (e.g., "MISSING_ACTIVE_PRICE", "ORDER_ALREADY_COMPLETED").
 *               This can be used in the 'details' map and potentially as a message parameter.
 * @param entityId Optional ID or identifier of the entity involved.
 * @param additionalDetails Optional map for further context-specific details.
 * @param parameters Vararg String parameters to be used for MessageSource formatting (can include reason if desired).
 * @return DomainException instance.
 */
fun createInvalidStateException(
    reason: String? = null,
    entityId: Any? = null,
    additionalDetails: Map<String, Any>? = null,
    vararg parameters: String
): DomainException {
    val detailsMap = mutableMapOf<String, Any>()
    reason?.let { detailsMap["reason"] = it }
    entityId?.let { detailsMap["entityId"] = it.toString() }
    additionalDetails?.let { detailsMap.putAll(it) }

    val internalMessage = "Invalid State" + (reason?.let { ": $it" } ?: "") + (entityId?.let { " for entity $it" } ?: "")

    return DomainException(
        errorCode = INVALID_STATE,
        details = detailsMap.toMap(),
        parameters = parameters,
        message = internalMessage
    )
}

/**
 * Creates a standard DomainException for generic invalid data errors.
 * Uses the INVALID_DATA error code.
 *
 * @param field Optional name of the field containing invalid data.
 * @param value Optional invalid value provided.
 * @param additionalDetails Optional map for further context.
 * @param parameters Vararg String parameters for MessageSource formatting.
 * @return DomainException instance.
 */
fun createInvalidDataException(
    field: String? = null,
    value: Any? = null,
    additionalDetails: Map<String, Any>? = null,
    vararg parameters: String
): DomainException {
    val detailsMap = mutableMapOf<String, Any>()
    field?.let { detailsMap["field"] = it }
    value?.let { detailsMap["value"] = it.toString() }
    additionalDetails?.let { detailsMap.putAll(it) }

    val internalMessage = "Invalid Data" + (field?.let { " for field '$it'" } ?: "")

    return DomainException(
        errorCode = INVALID_DATA,
        details = detailsMap.toMap(),
        parameters = parameters,
        message = internalMessage
    )
}
