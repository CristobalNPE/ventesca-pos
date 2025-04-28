package dev.cnpe.ventescaposbe.catalog.application.service

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class SkuGenerator {

    companion object {
        private const val DELIMITER = "-"
        private const val CODE_LENGTH = 6
        private const val UUID_LENGTH = 6
        private const val DATE_PATTERN = "yyyyMM"
        private const val PREFIX = "P"
    }

    /**
     * Generates a unique SKU based on product name, creation date, and a random ID.
     * Example: P-LAPTOP-202410-A1B2C3
     *
     * @param productName The name of the product.
     * @return A generated SKU string.
     */
    fun generateSku(productName: String?): String {
        require(!productName.isNullOrBlank()) { "Product name cannot be blank for SKU generation" }

        val productCode = generateCodeFromName(productName)
        val creationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN))
        val uniqueId = UUID.randomUUID().toString().replace("-", "").take(UUID_LENGTH)

        return "$PREFIX$DELIMITER$productCode$DELIMITER$creationDate$DELIMITER$uniqueId".uppercase()
    }

    /**
     * Generates a fixed-length code derived from the product name.
     * Removes special chars, vowels, takes first letters, pads if necessary.
     */
    private fun generateCodeFromName(name: String): String {
        // uppercase, remove non-alphanumeric
        val processedBase = name.uppercase().replace(Regex("[^A-Z0-9]"), "")

        // try removing vowels first
        var processed = processedBase.replace(Regex("[AEIOU]"), "")

        // if too short after removing vowels, revert to base without vowels removed
        if (processed.length < CODE_LENGTH) {
            processed = processedBase
        }

        // pad with 'X' if still too short
        val padded = processed.padEnd(CODE_LENGTH, 'X')

        return padded.take(CODE_LENGTH)
    }

}