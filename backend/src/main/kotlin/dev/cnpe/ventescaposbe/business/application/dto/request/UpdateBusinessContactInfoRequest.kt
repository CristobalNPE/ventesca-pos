package dev.cnpe.ventescaposbe.business.application.dto.request

import dev.cnpe.ventescaposbe.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL


@Schema(description = "Request to update business contact info (phone, email, website). Fields are optional.")
data class UpdateBusinessContactInfoRequest(

    @field:Size(min = 7, max = 25, message = "Phone number length invalid.")
    @Schema(description = "New primary contact phone number.", example = "+44 20 7123 4567")
    val phone: String?,

    @field:NotBlankIfPresent(message = "Email cannot be blank if provided.")
    @field:Email(message = "Invalid email format.")
    @Schema(description = "New primary contact email.", example = "sales@myupdatedstore.com")
    val email: String?,

    @field:NotBlankIfPresent(message = "Website URL cannot be blank if provided.")
    @field:URL(message = "Invalid URL format.")
    @field:Size(max = 255, message = "Website URL too long.")
    @Schema(description = "New official website URL.", example = "https://myupdatedstore.com")
    val website: String?
)