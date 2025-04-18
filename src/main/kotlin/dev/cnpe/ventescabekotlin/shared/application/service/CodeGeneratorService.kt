package dev.cnpe.ventescabekotlin.shared.application.service

import dev.cnpe.ventescabekotlin.shared.domain.vo.GeneratedCode
import org.springframework.stereotype.Service

@Service
class CodeGeneratorService {

    companion object {
        const val CODE_LENGTH = 3
    }

    fun generateCode(name: String): String {

        // why the non-null check here? we can just ask for the name not to be null in the fun params no?

        val words = name.split(Regex("\\s+|&|\\+|-")).filter { it.isNotEmpty() }

        val codeBuilder = StringBuilder(
            words.joinToString("") { it.first().uppercase() } // this syntax is weird to me, explain plase
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