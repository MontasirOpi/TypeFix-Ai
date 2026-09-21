package com.typefix.ai.correction

enum class CorrectionAction {
    FIX,
    IMPROVE,
    PROFESSIONAL,
    FRIENDLY,
    SHORTEN,
    EXPAND
}

data class ValidationResult(
    val isValid: Boolean,
    val rejectionReason: String? = null
)

/**
 * Validates that corrected text preserves meaning, entities, and safety boundaries.
 */
class SafetyValidator {

    fun validate(
        original: PreprocessedText,
        corrected: String,
        action: CorrectionAction,
        diff: DiffResult
    ): ValidationResult {
        // 1. Never allow empty/blank output when original text was non-empty
        if (original.normalizedText.isNotBlank() && corrected.isBlank()) {
            return ValidationResult(false, "Correction produced empty text")
        }

        // 2. Protect critical entities across all actions
        for (token in original.protectedTokens) {
            when (token.type) {
                TokenType.URL, TokenType.EMAIL, TokenType.CODE -> {
                    if (!corrected.contains(token.token, ignoreCase = true)) {
                        return ValidationResult(
                            false,
                            "Protected entity '${token.token}' was altered or omitted"
                        )
                    }
                }
                TokenType.NUMBER -> {
                    if (!corrected.contains(token.token)) {
                        return ValidationResult(
                            false,
                            "Numeric entity '${token.token}' was altered or omitted"
                        )
                    }
                }
                TokenType.MENTION -> {
                    if (!corrected.contains(token.token, ignoreCase = true)) {
                        return ValidationResult(
                            false,
                            "Mention '${token.token}' was altered or omitted"
                        )
                    }
                }
                TokenType.HASHTAG -> {
                    if (!corrected.contains(token.token, ignoreCase = true)) {
                        return ValidationResult(
                            false,
                            "Hashtag '${token.token}' was altered or omitted"
                        )
                    }
                }
            }
        }

        // 3. Specific validation for FIX mode (must not drift into creative rewriting)
        if (action == CorrectionAction.FIX) {
            // For longer sentences, reject if word similarity drops significantly
            if (original.normalizedText.length > 15 && original.protectedTokens.isEmpty()) {
                if (diff.wordSimilarityRatio < 0.50 && diff.characterEditRatio > 0.45) {
                    return ValidationResult(
                        false,
                        "Fix mode change exceeded allowable threshold (drift detected)"
                    )
                }
            }

            // Word count shouldn't balloon or collapse dramatically in Fix mode
            val origWords = diff.originalWordCount
            val corrWords = diff.correctedWordCount
            if (origWords > 3) {
                if (corrWords > origWords * 1.6 || corrWords < origWords * 0.6) {
                    return ValidationResult(
                        false,
                        "Fix mode word count drifted excessively ($origWords -> $corrWords)"
                    )
                }
            }
        }

        return ValidationResult(true)
    }
}
