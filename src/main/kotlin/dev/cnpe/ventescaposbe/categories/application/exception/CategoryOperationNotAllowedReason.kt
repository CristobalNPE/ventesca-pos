package dev.cnpe.ventescaposbe.categories.application.exception

import dev.cnpe.ventescaposbe.shared.application.exception.OperationNotAllowedReason
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reasons why a category operation is not allowed.")
enum class CategoryOperationNotAllowedReason: OperationNotAllowedReason {

    @Schema(description = "The category is the default category.")
    IS_DEFAULT_CATEGORY,

    @Schema(description = "The category has subcategories.")
    HAS_SUBCATEGORIES,

    @Schema(description = "Cannot add a subcategory to the default category.")
    ADD_SUBCATEGORY_TO_DEFAULT,

    @Schema(description = "The category has reached the maximum depth.")
    REACHED_MAX_DEPTH
}