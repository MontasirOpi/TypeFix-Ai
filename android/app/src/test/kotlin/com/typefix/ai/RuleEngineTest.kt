package com.typefix.ai

import com.typefix.ai.correction.RuleEngine
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RuleEngineTest {

    private lateinit var ruleEngine: RuleEngine

    @Before
    fun setUp() {
        ruleEngine = RuleEngine()
    }

    @Test
    fun testCommonTypos() {
        assertEquals("the", ruleEngine.correct("teh"))
        assertEquals("receive", ruleEngine.correct("recieve"))
        assertEquals("definitely", ruleEngine.correct("definately"))
        assertEquals("tomorrow", ruleEngine.correct("tomorow"))
        assertEquals("going", ruleEngine.correct("goign"))
        assertEquals("because", ruleEngine.correct("becuase"))
    }

    @Test
    fun testContractions() {
        assertEquals("don't", ruleEngine.correct("dont"))
        assertEquals("can't", ruleEngine.correct("cant"))
        assertEquals("won't", ruleEngine.correct("wont"))
        assertEquals("I'm", ruleEngine.correct("im"))
        assertEquals("that's", ruleEngine.correct("thats"))
    }

    @Test
    fun testPronounAndCapitalization() {
        assertEquals("I am happy", ruleEngine.correct("i am happy"))
        assertEquals("Today is Monday.", ruleEngine.correct("today is monday."))
    }

    @Test
    fun testCasingPreservation() {
        assertEquals("The", ruleEngine.correct("Teh"))
        assertEquals("THE", ruleEngine.correct("TEH"))
        assertEquals("Don't", ruleEngine.correct("Dont"))
    }

    @Test
    fun testPunctuationAndSpacing() {
        assertEquals("Hello, world", ruleEngine.correct("Hello , world"))
        assertEquals("Hello, world", ruleEngine.correct("Hello,world"))
    }

    @Test
    fun testComplexSentence() {
        val input = "i am goign to office tomorow"
        val expected = "I am going to office tomorrow"
        assertEquals(expected, ruleEngine.correct(input))
    }
}
