package dev.cnpe.ventescaposbe.business.application.dto.request

import dev.cnpe.ventescaposbe.business.domain.enums.PaymentMethod
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.math.BigDecimal


@Schema(description = "Data provided by a Superuser to register a new Business and its initial Admin user.")
data class AdminCreateBusinessRequest(

    // --- Details for NEW Business Admin User ---
    @field:NotBlank
    @field:Email
    @Schema(
        description = "Email address for the initial Business Admin user (must be unique in IdP).",
        example = "admin@newstore.com",
        required = true
    )
    val adminUserEmail: String,

    @Schema(description = "Optional initial password for the Business Admin user.", example = "TempPass123!")
    val adminUserInitialPassword: String? = null,

    @Schema(description = "First name for the Business Admin user.", example = "Store")
    val adminUserFirstName: String? = null,

    @Schema(description = "Last name for the Business Admin user.", example = "Owner")
    val adminUserLastName: String? = null,

    @Schema(
        description = "Optional username for the Business Admin user (if different from email).",
        example = "store_owner_admin"
    )
    val adminUserUsername: String? = null,

    // --- Business Details ---
    @field:NotBlank
    @field:Size(min = 2, max = 50)
    @Schema(description = "The official name of the new business.", example = "New Corner Store", required = true)
    val businessName: String,

    @field:Size(max = 500)
    @Schema(
        description = "Optional short brand message or slogan for the business.",
        example = "Your friendly neighborhood store"
    )
    val brandMessage: String?,

    // --- Business Configuration ---
    @field:NotBlank
    @field:Pattern(regexp = "^[A-Z]{3}$", message = "Must be a 3-letter uppercase ISO 4217 code")
    @Schema(description = "Default currency code (ISO 4217) for the business.", example = "USD", required = true)
    val currencyCode: String, // TODO: Validate against Currency table?

    @field:NotNull
    @field:DecimalMin(value = "0.0", inclusive = true)
    @field:DecimalMax(value = "100.0", inclusive = true)
    @field:Digits(integer = 3, fraction = 2)
    @Schema(description = "Default tax percentage (0-100).", example = "7.5", required = true)
    val taxPercentage: BigDecimal,

    @field:NotNull
    @field:Size(min = 1, message = "At least one payment method is required")
    @Schema(
        description = "Set of initially accepted payment methods.",
        example = "[\"CASH\", \"CREDIT_CARD\"]",
        required = true
    )
    val acceptedPaymentMethods: Set<PaymentMethod>, // Set of Enums

    // --- Business Contact Info ---
    @field:Size(min = 7, max = 25)
    @Schema(description = "Optional primary contact phone number for the business.", example = "+1-555-867-5309")
    val contactPhone: String?,

    @field:Email
    @Schema(
        description = "Optional primary contact email for the business (can differ from admin email).",
        example = "info@newcornerstore.com"
    )
    val contactEmail: String?,

    @field:org.hibernate.validator.constraints.URL // Use specific annotation if needed
    @Schema(description = "Optional official website URL for the business.", example = "https://newcornerstore.com")
    val contactWebsite: String?,

    // --- Initial Main Branch Details ---
    @Schema(description = "Optional name for the main branch (defaults if blank).", example = "Main Store Location")
    val mainBranchName: String? = null,

    @Schema(description = "Street address for the main branch.", example = "123 Market St")
    val mainBranchStreet: String?,

    @Schema(description = "City for the main branch.", example = "Metropolis")
    val mainBranchCity: String?,

    @Schema(description = "ZIP/Postal code for the main branch.", example = "12345")
    val mainBranchZipCode: String?,

    @Schema(description = "Country for the main branch.", example = "USA")
    val mainBranchCountry: String?
)