package com.typefix.ai.correction

import java.util.regex.Pattern

/**
 * Metadata captured during preprocessing to restore formatting and validate tokens.
 */
data class PreprocessedText(
    val normalizedText: String,
    val leadingWhitespace: String,
    val trailingWhitespace: String,
    val protectedTokens: List<ProtectedToken>,
    val isAllCaps: Boolean = false,
    val isTitleCase: Boolean = false
)

data class ProtectedToken(
    val token: String,
    val type: TokenType,
    val startIndex: Int,
    val endIndex: Int
)

enum class TokenType {
    URL,
    EMAIL,
    MENTION,
    HASHTAG,
    NUMBER,
    CODE
}

/**
 * Preprocesses raw input text, identifying protected tokens and formatting.
 */
class TextPreprocessor {

    companion object {
        private val URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+",
            Pattern.CASE_INSENSITIVE
        )
        private val EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        )
        private val MENTION_PATTERN = Pattern.compile("@[a-zA-Z0-9_]+")
        private val HASHTAG_PATTERN = Pattern.compile("#[a-zA-Z0-9_]+")
        private val NUMBER_PATTERN = Pattern.compile("\\b\\d+(?:[.,]\\d+)?\\b")
        private val CODE_PATTERN = Pattern.compile("`[^`]+`|\\w+\\(\\)")
    }

    fun preprocess(rawText: String): PreprocessedText {
        if (rawText.isEmpty()) {
            return PreprocessedText("", "", "", emptyList())
        }

        val leadingWhitespace = rawText.takeWhile { it.isWhitespace() }
        val trailingWhitespace = rawText.takeLastWhile { it.isWhitespace() }
        val trimmed = rawText.substring(
            leadingWhitespace.length,
            rawText.length - trailingWhitespace.length
        )

        val protectedTokens = extractProtectedTokens(trimmed)
        val isAllCaps = trimmed.length > 2 && trimmed.all { !it.isLetter() || it.isUpperCase() }

        return PreprocessedText(
            normalizedText = trimmed,
            leadingWhitespace = leadingWhitespace,
            trailingWhitespace = trailingWhitespace,
            protectedTokens = protectedTokens,
            isAllCaps = isAllCaps
        )
    }

    fun extractProtectedTokens(text: String): List<ProtectedToken> {
        val tokens = mutableListOf<ProtectedToken>()

        fun addMatches(pattern: Pattern, type: TokenType) {
            val matcher = pattern.matcher(text)
            while (matcher.find()) {
                tokens.add(
                    ProtectedToken(
                        token = matcher.group(),
                        type = type,
                        startIndex = matcher.start(),
                        endIndex = matcher.end()
                    )
                )
            }
        }

        addMatches(URL_PATTERN, TokenType.URL)
        addMatches(EMAIL_PATTERN, TokenType.EMAIL)
        addMatches(MENTION_PATTERN, TokenType.MENTION)
        addMatches(HASHTAG_PATTERN, TokenType.HASHTAG)
        addMatches(NUMBER_PATTERN, TokenType.NUMBER)
        addMatches(CODE_PATTERN, TokenType.CODE)

        return tokens.sortedBy { it.startIndex }
    }
}
