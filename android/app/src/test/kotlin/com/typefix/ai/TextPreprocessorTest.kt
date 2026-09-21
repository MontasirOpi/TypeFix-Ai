package com.typefix.ai

import com.typefix.ai.correction.TextPreprocessor
import com.typefix.ai.correction.TokenType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TextPreprocessorTest {

    private lateinit var preprocessor: TextPreprocessor

    @Before
    fun setUp() {
        preprocessor = TextPreprocessor()
    }

    @Test
    fun testProtectedTokens() {
        val input = "Check https://github.com and contact dev@example.com or @john #typefix at 4.5"
        val tokens = preprocessor.extractProtectedTokens(input)

        assertTrue(tokens.any { it.type == TokenType.URL && it.token == "https://github.com" })
        assertTrue(tokens.any { it.type == TokenType.EMAIL && it.token == "dev@example.com" })
        assertTrue(tokens.any { it.type == TokenType.MENTION && it.token == "@john" })
        assertTrue(tokens.any { it.type == TokenType.HASHTAG && it.token == "#typefix" })
        assertTrue(tokens.any { it.type == TokenType.NUMBER && it.token == "4.5" })
    }

    @Test
    fun testWhitespacePreservation() {
        val input = "   hello world \n"
        val meta = preprocessor.preprocess(input)

        assertEquals("   ", meta.leadingWhitespace)
        assertEquals(" \n", meta.trailingWhitespace)
        assertEquals("hello world", meta.normalizedText)
    }
}
