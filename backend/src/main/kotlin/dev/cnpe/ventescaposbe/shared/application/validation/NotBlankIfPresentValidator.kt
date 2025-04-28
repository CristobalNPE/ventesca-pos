package dev.cnpe.ventescaposbe.shared.application.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class NotBlankIfPresentValidator : ConstraintValidator<NotBlankIfPresent, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value == null || value.isNotBlank()
    }

}