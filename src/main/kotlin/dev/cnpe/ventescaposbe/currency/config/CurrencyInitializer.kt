package dev.cnpe.ventescaposbe.currency.config

import dev.cnpe.ventescaposbe.currency.domain.model.Currency
import dev.cnpe.ventescaposbe.currency.infrastructure.persistence.CurrencyRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

/**
 * Configuration class responsible for initializing necessary currency data on application startup.
 */
@Configuration
class CurrencyInitializer {

    /**
     * Creates a CommandLineRunner bean that initializes default currencies if none exist.
     * Using CommandLineRunner ensures this runs after the context is loaded.
     *
     * @param currencyRepository The repository for currency data access.
     * @return A CommandLineRunner bean.
     */
    @Bean
    @Order(10)
    fun initializeCurrenciesRunner(currencyRepository: CurrencyRepository): CommandLineRunner {
        return CommandLineRunner {
            log.info { "Checking for existing currencies..." }
            initializeDefaultCurrency(currencyRepository)
        }
    }

    @Transactional
    internal fun initializeDefaultCurrency(currencyRepository: CurrencyRepository) {
        if (currencyRepository.count() == 0L) {
            log.info { "No currencies found. Initializing default currencies..." }

            val clp = Currency(
                code = "CLP",
                name = "Chilean Peso",
                symbol = "$",
                scale = 0,
                isActive = true
            )

            val currenciesToInitialize = listOf(clp)
            currencyRepository.saveAll(currenciesToInitialize)
            log.info { "✅💲 ${currenciesToInitialize.size} default currencies initialized. {${currenciesToInitialize.joinToString { it.code }}}" }
        } else {
            log.info { "Currencies already exist in database (${currencyRepository.count()} found). Skipping initialization." }
        }
    }
}