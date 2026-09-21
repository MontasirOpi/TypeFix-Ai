package com.typefix.ai

import com.typefix.ai.correction.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SafetyValidatorTest {

    private lateinit var validator: SafetyValidator
    private lateinit var preprocessor: TextPreprocessor
    private lateinit var diffEngine: DiffEngine

    @Before
    fun setUp() {
        validator = SafetyValidator()
        preprocessor = TextPreprocessor()
        diffEngine = DiffEngine()
    }

    @Test
    fun testProtectUrlAndEmail() {
        val original = "Send file to dev@test.com via https://secure.com immediately."
        val meta = preprocessor.preprocess(original)

        // Corrupted candidate that alters the email
        val corrupted = "Send file to user@another.com via https://secure.com immediately."
        val diff = diffEngine.computeDiff(original, corrupted)
        val result = validator.validate(meta, corrupted, CorrectionAction.FIX, diff)

        assertFalse("Should reject because email was altered", result.isValid)
    }

    @Test
    fun testRejectExcessiveDriftInFixMode() {
        val original = "I need the file tomorrow."
        val meta = preprocessor.preprocess(original)

        // Extreme drift that changes meaning/tone into a formal letter
        val excessive = "I am writing to formally request that you kindly provide the document at your earliest convenience."
        val diff = diffEngine.computeDiff(original, excessive)
        val result = validator.validate(meta, excessive, CorrectionAction.FIX, diff)

        assertFalse("Should reject excessive drift for FIX mode", result.isValid)
    }

    @Test
    fun testAcceptLegitimateFix() {
        val original = "i am goign to office tomorow"
        val meta = preprocessor.preprocess(original)
        val corrected = "I am going to office tomorrow"
        val diff = diffEngine.computeDiff(original, corrected)
        val result = validator.validate(meta, corrected, CorrectionAction.FIX, diff)

        assertTrue("Legitimate fix should be accepted", result.isValid)
    }
}
