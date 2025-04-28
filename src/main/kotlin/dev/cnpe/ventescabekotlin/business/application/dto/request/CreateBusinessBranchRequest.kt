package dev.cnpe.ventescabekotlin.business.application.dto.request

import dev.cnpe.ventescabekotlin.shared.application.validation.NotBlankIfPresent
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "Request to create a new business branch.")
data class CreateBusinessBranchRequest(

    //  If not provided, the creator (admin) might be assigned, or it could be required later.
    @Schema(description = "Optional IdP User ID of the user designated as Branch Manager.", example = "auth0|65...")
    val managerId: String?,

    @field:NotBlankIfPresent(message = "Branch name cannot be blank if provided.")
    @field:Size(min = 2, max = 50, message = "Branch name must be between 2 and 50 characters.")
    @Schema(description = "Optional name for the new branch (defaults if blank).", example = "Downtown Branch")
    val branchName: String?,

    @field:Size(min = 2, max = 100)
    @Schema(description = "Street address for the branch.", example = "456 Side St")
    val addressStreet: String?,

    @field:Size(min = 2, max = 50)
    @Schema(description = "City for the branch.", example = "Metropolis")
    val addressCity: String?,

    @field:Size(min = 3, max = 20)
    @Schema(description = "ZIP/Postal code for the branch.", example = "54321")
    val addressZipCode: String?,

    @field:Size(min = 2, max = 50)
    @Schema(description = "Country for the branch.", example = "USA")
    val addressCountry: String?,

    @field:Size(min = 7, max = 25)
    @Schema(description = "Optional primary contact phone number for the branch.", example = "+1-555-987-6543")
    val contactNumber: String?
)