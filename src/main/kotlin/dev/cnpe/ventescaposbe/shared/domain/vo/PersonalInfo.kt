package dev.cnpe.ventescaposbe.shared.domain.vo

import dev.cnpe.ventescaposbe.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

@Schema(description = "Represents personal identification information.")
@Embeddable
data class PersonalInfo(

    @field:Schema(
        description = "First or given name.",
        example = "Jane",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @field:Column(name = "first_name", nullable = false)
    @field:NotBlank
    @field:Length(min = 1, max = 50)
    val firstName: String,

    @field:Schema(
        description = "Last or family name.",
        example = "Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @field:Column(name = "last_name")
    @field:NotBlankIfPresent
    @field:Length(min = 1, max = 50)
    val lastName: String?,

    @field:Schema(
        description = "Personal identification number (e.g., national ID, employee ID).",
        example = "ID987654",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @field:Column(name = "personal_id")
    @field:NotBlankIfPresent
    @field:Length(min = 2, max = 50)
    val personalId: String?
){
    companion object {
        fun empty(): PersonalInfo = PersonalInfo("", null, null)
    }
}