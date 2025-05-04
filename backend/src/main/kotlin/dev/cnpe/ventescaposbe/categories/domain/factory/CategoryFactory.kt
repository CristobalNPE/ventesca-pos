package dev.cnpe.ventescaposbe.categories.domain.factory

import dev.cnpe.ventescaposbe.categories.domain.model.Category
import dev.cnpe.ventescaposbe.categories.infrastructure.persistence.CategoryRepository
import dev.cnpe.ventescaposbe.shared.application.service.CodeGeneratorService
import dev.cnpe.ventescaposbe.shared.domain.vo.GeneratedCode
import org.springframework.stereotype.Component
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private const val DEFAULT_CATEGORY_COLOR = "607d8b"

@Component
class CategoryFactory(
    private val codeGeneratorService: CodeGeneratorService,
    private val categoryRepository: CategoryRepository
) {

    fun create(name: String): Category {
        val sanitized = name.replaceFirstChar { it.titlecase() }.trim()
        val uniqueCode = ensureUniqueCode(codeGeneratorService.generateCode(sanitized))

        return Category(
            name = sanitized,
            color = generateHexColor(),
            code = GeneratedCode(uniqueCode)
        )
    }

    fun createSubcategory(name: String, parent: Category): Category {
        val sanitized = name.replaceFirstChar { it.titlecase() }.trim()

        val uniqueSubCode = ensureUniqueCode(codeGeneratorService.generateCode(sanitized))
        val fullCode = "${parent.code.codeValue}-$uniqueSubCode"
        val color = generateSubcategoryColor(parent.color)

        val newSubcategory = Category(
            name = sanitized,
            color = color,
            parent = parent,
            code = GeneratedCode(fullCode)
        )
        parent.addSubcategory(newSubcategory)
        return newSubcategory
    }

    fun createDefault(name: String): Category {
        val code = codeGeneratorService.generateCode(name)

        return Category(
            name = name,
            color = DEFAULT_CATEGORY_COLOR,
            code = GeneratedCode(code),
            isDefault = true
        )
    }

    private fun generateHexColor(): String {
        val hue = Random.nextFloat()
        val saturation = 0.4f
        val brightness = 0.8f

        val color = Color.getHSBColor(hue, saturation, brightness)
        return String.format("#%06X", color.rgb and 0xFFFFFF)
    }

    private fun generateSubcategoryColor(parentColorHex: String): String {
        return try {
            val parentColor = Color.decode(parentColorHex)
            val hsbValues = Color.RGBtoHSB(parentColor.red, parentColor.green, parentColor.blue, null)

            val brightnessDelta = Random.nextDouble(0.05, 0.20).toFloat()

            val newBrightness = min(hsbValues[2] + brightnessDelta, 0.95f)
            val newColor = Color.getHSBColor(hsbValues[0], hsbValues[1], newBrightness)
            String.format("#%06X", newColor.rgb and 0xFFFFFF)
        } catch (e: NumberFormatException) {
            generateHexColor()
        }
    }

    private fun ensureUniqueCode(baseCode: String): String {
        var currentCode = baseCode
        var suffix = 1
        val maxAttempts = 36

        while (categoryRepository.existsByCode(GeneratedCode(currentCode)) && suffix <= maxAttempts) {
            val codeRoot = baseCode.take(max(0, currentCode.length - 1))
            currentCode = if (suffix <= 9) {
                codeRoot + suffix
            } else {
                codeRoot + ('A'.code + (suffix - 10)).toChar()
            }
            suffix++
        }
        if (suffix > maxAttempts) {
            //Consider generating a random code?
            throw IllegalStateException("Unable to generate unique code for category with base code $baseCode")
        }
        return currentCode
    }

}


