package dev.cnpe.ventescabekotlin.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.*

@Configuration
class MessageSourceConfig {

    @Bean
    fun messageSource(): MessageSource {
        return ReloadableResourceBundleMessageSource().apply {
            setBasenames(
                "classpath:messages/errors",
                "classpath:messages/enums",
                "classpath:messages/defaults"
            )
            setDefaultEncoding("UTF-8")
            setUseCodeAsDefaultMessage(true)
        }
    }

    @Bean
    fun localeResolver(): LocaleResolver {
        return AcceptHeaderLocaleResolver().apply {
            supportedLocales = listOf(Locale.ENGLISH, Locale.of("es"))
            setDefaultLocale(Locale.of("es"))
        }
    }

}