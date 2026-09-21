package com.typefix.ai.correction

import com.typefix.ai.ai.LocalModelEngine

data class CorrectionResult(
    val originalText: String,
    val correctedText: String,
    val action: CorrectionAction,
    val diff: DiffResult,
    val isSuccess: Boolean,
    val usedAI: Boolean,
    val message: String? = null
)

/**
 * Modular correction coordinator executing preprocessing, rules, AI inference, diffing, and validation.
 */
class CorrectionEngine(
    private val textPreprocessor: TextPreprocessor = TextPreprocessor(),
    private val ruleEngine: RuleEngine = RuleEngine(),
    private val spellChecker: SpellChecker = SpellChecker(),
    private val localModelEngine: LocalModelEngine? = null,
    private val postProcessor: PostProcessor = PostProcessor(),
    private val diffEngine: DiffEngine = DiffEngine(),
    private val safetyValidator: SafetyValidator = SafetyValidator()
) {

    suspend fun process(
        rawText: String,
        action: CorrectionAction = CorrectionAction.FIX
    ): CorrectionResult {
        if (rawText.isBlank()) {
            val emptyDiff = diffEngine.computeDiff(rawText, rawText)
            return CorrectionResult(
                originalText = rawText,
                correctedText = rawText,
                action = action,
                diff = emptyDiff,
                isSuccess = true,
                usedAI = false
            )
        }

        // 1. Preprocess
        val preprocessed = textPreprocessor.preprocess(rawText)
        var usedAI = false
        var candidateText: String

        if (action == CorrectionAction.FIX) {
            // 2. Fast deterministic rule engine first
            val ruleResult = ruleEngine.correct(preprocessed.normalizedText)

            // 3. Local AI model if available and needed
            if (localModelEngine != null && localModelEngine.isLoaded()) {
                try {
                    val aiResult = localModelEngine.correctText(ruleResult)
                    if (aiResult.isNotBlank()) {
                        candidateText = aiResult
                        usedAI = true
                    } else {
                        candidateText = ruleResult
                    }
                } catch (_: Exception) {
                    candidateText = ruleResult
                }
            } else {
                candidateText = ruleResult
            }
        } else {
            // Rewrite actions (Improve, Professional, Friendly, Shorten, Expand)
            if (localModelEngine != null) {
                try {
                    val aiResult = localModelEngine.rewriteText(preprocessed.normalizedText, action.name)
                    candidateText = if (aiResult.isNotBlank()) {
                        usedAI = true
                        aiResult
                    } else {
                        preprocessed.normalizedText
                    }
                } catch (_: Exception) {
                    candidateText = preprocessed.normalizedText
                }
            } else {
                candidateText = preprocessed.normalizedText
            }
        }

        // 4. Post-processing (restore whitespace, casing)
        val finalCandidate = postProcessor.postProcess(preprocessed, candidateText)

        // 5. Diff computation
        val diff = diffEngine.computeDiff(rawText, finalCandidate)

        // 6. Safety and meaning validation
        val validation = safetyValidator.validate(preprocessed, finalCandidate, action, diff)

        return if (validation.isValid) {
            CorrectionResult(
                originalText = rawText,
                correctedText = finalCandidate,
                action = action,
                diff = diff,
                isSuccess = true,
                usedAI = usedAI,
                message = "Text processed successfully"
            )
        } else {
            // Safety fallback: If AI failed safety validation, attempt safe rule result
            val safeRuleText = postProcessor.postProcess(
                preprocessed,
                ruleEngine.correct(preprocessed.normalizedText)
            )
            val ruleDiff = diffEngine.computeDiff(rawText, safeRuleText)
            val ruleValidation = safetyValidator.validate(
                preprocessed,
                safeRuleText,
                CorrectionAction.FIX,
                ruleDiff
            )

            if (ruleValidation.isValid) {
                CorrectionResult(
                    originalText = rawText,
                    correctedText = safeRuleText,
                    action = action,
                    diff = ruleDiff,
                    isSuccess = true,
                    usedAI = false,
                    message = "Fallback to rule correction: ${validation.rejectionReason}"
                )
            } else {
                // Absolute fallback: preserve original unchanged
                CorrectionResult(
                    originalText = rawText,
                    correctedText = rawText,
                    action = action,
                    diff = diffEngine.computeDiff(rawText, rawText),
                    isSuccess = false,
                    usedAI = false,
                    message = "Rejected for safety: ${validation.rejectionReason}"
                )
            }
        }
    }
}
