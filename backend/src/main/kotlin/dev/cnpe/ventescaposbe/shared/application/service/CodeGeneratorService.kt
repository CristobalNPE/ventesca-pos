package dev.cnpe.ventescaposbe.shared.application.service

import org.springframework.stereotype.Service

@Service
class CodeGeneratorService {


    fun generateCode(name: String, codeLength: Int = 3): String {

        require(name.isNotBlank()) { "Name cannot be blank." }

        val words = name.split(Regex("\\s+|&|\\+|-")).filter { it.isNotEmpty() }

        val codeBuilder = StringBuilder(
            words.joinToString("") { it.first().uppercase() }
        )

        if (codeBuilder.length < codeLength && words.isNotEmpty()) {
            val firstWord = words[0]
            var i = 1
            while (codeBuilder.length < codeLength && i < firstWord.length) {
                codeBuilder.append(firstWord[i].uppercaseChar())
                i++
            }
        }

        while (codeBuilder.length < codeLength) {
            codeBuilder.append("X")
        }

        return codeBuilder.toString().take(codeLength)
    }


}