package dev.cnpe.ventescabekotlin.shared.infrastructure.web

import dev.cnpe.ventescabekotlin.business.domain.enums.BusinessStatus
import dev.cnpe.ventescabekotlin.shared.application.dto.EnumInfoResponse
import dev.cnpe.ventescabekotlin.shared.application.dto.EnumValueInfo
import dev.cnpe.ventescabekotlin.shared.application.service.EnumTranslationService
import dev.cnpe.ventescabekotlin.shared.domain.enums.DomainEnum
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/enums")
@Tag(name = "Enumerations", description = "Provides definitions and localized text for application enums.")
class EnumInfoController(
    private val enumTranslationService: EnumTranslationService
) {

    // Need to MANUALLY register each enum to expose
    private val enumRegistry: Map<String, Class<out DomainEnum>> = mapOf(
        "business-status" to BusinessStatus::class.java
    ).mapKeys { it.key.lowercase() }


    @GetMapping("/{enumName}")
    @Operation(summary = "Get details for a specific enum type")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Enum details found",
                content = [Content(schema = Schema(implementation = EnumInfoResponse::class))]
            ),
            ApiResponse(responseCode = "404", description = "Enum type not found or not exposed")
        ]
    )
    fun getEnumInfo(
        @Parameter(
            description = "The kebab-case name of the enum type (e.g., 'business-status', 'payment-method')",
            required = true
        )
        @PathVariable(name = "enumName") enumName: String
    ): ResponseEntity<EnumInfoResponse> {

        val enumClass = enumRegistry[enumName.lowercase()]
            ?: return ResponseEntity.notFound().build()

        if (!DomainEnum::class.java.isAssignableFrom(enumClass)) {
            log.error { "Enum class ${enumClass.simpleName} found in registry but does not implement DomainEnum." }
            return ResponseEntity.status(500).build()
        }
        val locale = LocaleContextHolder.getLocale()
        log.debug { "Fetching enum info for ${enumClass.simpleName} with locale $locale" }

        val enumValues = enumClass.enumConstants as Array<DomainEnum>

        val valueInfoList = enumValues.map { enumConstant ->
            EnumValueInfo(
                value = enumConstant.name,
                name = enumTranslationService.getReadableName(enumConstant, locale),
                description = enumTranslationService.getDescription(enumConstant, locale)
            )
        }

        val response = EnumInfoResponse(
            enumName = enumClass.simpleName,
            values = valueInfoList
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping
    @Operation(summary = "List all exposed enum types")
    @ApiResponse(
        responseCode = "200",
        description = "List of available enum names",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = String::class, type = "array"),
        )]
    )
    fun listExposedEnums(): Set<String> {
        return enumRegistry.keys
    }

}