package dev.cnpe.ventescaposbe.shared.application.service

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import dev.cnpe.ventescaposbe.shared.domain.enums.messageKeyPrefix
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class EnumTranslationService(
    private val messageSource: MessageSource
) {

    companion object {
        private const val NAME_SUFFIX = ".name"
        private const val DESCRIPTION_SUFFIX = ".description"
    }

    /**
     * Gets the localized readable name for a domain enum constant.
     * Uses the current request's locale.
     * Falls back to a cleaned-up version of the enum constant name if translation is missing.
     */
    fun getReadableName(enumConstant: DomainEnum, locale: Locale = LocaleContextHolder.getLocale()): String {
        val key = enumConstant.messageKeyPrefix() + NAME_SUFFIX
        val defaultMessage = enumConstant.name.toReadableDefault()

        return try {
            messageSource.getMessage(key, null, defaultMessage, locale)!!
        } catch (e: NoSuchMessageException) {
            log.trace { "Missing message key [$key] for locale [$locale]. Using default '$defaultMessage'." }
            defaultMessage
        } catch (e: Exception) {
            log.error(e) { "Error resolving message for key [$key]" }
            defaultMessage
        }
    }

    /**
     * Gets the localized description for a domain enum constant.
     * Uses the current request's locale.
     * Returns null if the translation is missing.
     */
    fun getDescription(enumConstant: DomainEnum, locale: Locale = LocaleContextHolder.getLocale()): String? {
        val key = enumConstant.messageKeyPrefix() + DESCRIPTION_SUFFIX
        return try {
            messageSource.getMessage(key, null, null, locale)
        } catch (e: NoSuchMessageException) {
            log.trace { "Missing message key [$key] for locale [$locale]. Returning null description." }
            null
        } catch (e: Exception) {
            log.error(e) { "Error resolving message for key [$key]" }
            null
        }
    }

    private fun String.toReadableDefault(): String {
        return this.replace('_', ' ').lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}