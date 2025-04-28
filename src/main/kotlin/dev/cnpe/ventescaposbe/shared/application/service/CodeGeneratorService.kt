package dev.cnpe.ventescaposbe.shared.application.service

import org.springframework.stereotype.Service

@Service
class CodeGeneratorService {

    companion object {
        const val CODE_LENGTH = 3
    }

    fun generateCode(name: String): String {

        require(name.isNotBlank()) { "Name cannot be blank." }

        val words = name.split(Regex("\\s+|&|\\+|-")).filter { it.isNotEmpty() }

        val codeBuilder = StringBuilder(
            words.joinToString("") { it.first().uppercase() }
        )

        if (codeBuilder.length < CODE_LENGTH && words.isNotEmpty()) {
            val firstWord = words[0]
            var i = 1
            while (codeBuilder.length < CODE_LENGTH && i < firstWord.length) {
                codeBuilder.append(firstWord[i].uppercaseChar())
                i++
            }
        }

        while (codeBuilder.length < CODE_LENGTH) {
            codeBuilder.append("X")
        }

        return codeBuilder.toString().take(CODE_LENGTH)
    }


}