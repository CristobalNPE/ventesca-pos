package dev.cnpe.ventescaposbe.shared.domain.vo

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Schema(description = "Represents a system-generated code.")
@Embeddable
data class GeneratedCode(

    @Schema(
        description = "The value of the generated code.",
        example = "CAT-ELEC",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Column(name = "code", columnDefinition = "VARCHAR", nullable = false)
    val codeValue: String

)