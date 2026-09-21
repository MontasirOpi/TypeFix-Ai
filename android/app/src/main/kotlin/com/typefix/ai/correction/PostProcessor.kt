package com.typefix.ai.correction

import java.util.Locale

/**
 * Applies final formatting and restores original whitespace and casing styles.
 */
class PostProcessor {

    fun postProcess(
        originalMeta: PreprocessedText,
        correctedText: String
    ): String {
        var text = correctedText.trim()

        // Restore ALL CAPS if the entire original input was uppercase
        if (originalMeta.isAllCaps) {
            text = text.uppercase(Locale.ROOT)
        }

        // Reattach leading and trailing whitespace
        return originalMeta.leadingWhitespace + text + originalMeta.trailingWhitespace
    }
}
