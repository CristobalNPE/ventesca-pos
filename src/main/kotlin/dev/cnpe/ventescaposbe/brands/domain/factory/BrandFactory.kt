package dev.cnpe.ventescaposbe.brands.domain.factory

import dev.cnpe.ventescaposbe.brands.domain.model.Brand
import dev.cnpe.ventescaposbe.shared.application.service.CodeGeneratorService
import dev.cnpe.ventescaposbe.shared.domain.vo.GeneratedCode
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

@Component
class BrandFactory(private val codeGeneratorService: CodeGeneratorService) {

    fun create(name: String): Brand {
        val (sanitizedName, generatedCode) = prepareNameAndCode(name)

        return Brand(
            name = sanitizedName,
            code = generatedCode
        )
    }

    fun createDefault(name: String): Brand {
        val (sanitizedName, generatedCode) = prepareNameAndCode(name)

        return Brand(
            name = sanitizedName,
            code = generatedCode,
            isDefault = true
        )
    }

    private fun prepareNameAndCode(name: String): Pair<String, GeneratedCode> {
        val sanitized = StringUtils.capitalize(name).trim()
        val codeValue = codeGeneratorService.generateCode(sanitized)
        val generatedCode = GeneratedCode(codeValue)

        return Pair(sanitized, generatedCode)
    }
}