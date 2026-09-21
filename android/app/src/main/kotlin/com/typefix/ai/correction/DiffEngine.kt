package com.typefix.ai.correction

import kotlin.math.max
import kotlin.math.min

data class DiffResult(
    val originalWordCount: Int,
    val correctedWordCount: Int,
    val characterEditDistance: Int,
    val wordSimilarityRatio: Double,
    val characterEditRatio: Double,
    val isIdentical: Boolean
)

/**
 * Computes word-level and character-level differences between original and proposed text.
 */
class DiffEngine {

    fun computeDiff(original: String, corrected: String): DiffResult {
        if (original == corrected) {
            val words = countWords(original)
            return DiffResult(
                originalWordCount = words,
                correctedWordCount = words,
                characterEditDistance = 0,
                wordSimilarityRatio = 1.0,
                characterEditRatio = 0.0,
                isIdentical = true
            )
        }

        val origWords = tokenizeWords(original)
        val corrWords = tokenizeWords(corrected)

        val origWordCount = origWords.size
        val corrWordCount = corrWords.size

        // Character Levenshtein distance
        val charDistance = levenshtein(original, corrected)
        val maxLen = max(original.length, corrected.length)
        val charEditRatio = if (maxLen > 0) charDistance.toDouble() / maxLen else 0.0

        // Word overlap similarity (Jaccard-like or LCS)
        val wordSimilarity = computeWordSimilarity(origWords, corrWords)

        return DiffResult(
            originalWordCount = origWordCount,
            correctedWordCount = corrWordCount,
            characterEditDistance = charDistance,
            wordSimilarityRatio = wordSimilarity,
            characterEditRatio = charEditRatio,
            isIdentical = false
        )
    }

    private fun tokenizeWords(text: String): List<String> {
        return text.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { it.lowercase() }
    }

    private fun countWords(text: String): Int {
        return tokenizeWords(text).size
    }

    private fun computeWordSimilarity(orig: List<String>, corr: List<String>): Double {
        if (orig.isEmpty() && corr.isEmpty()) return 1.0
        if (orig.isEmpty() || corr.isEmpty()) return 0.0

        val origSet = orig.toSet()
        val corrSet = corr.toSet()

        val intersection = origSet.intersect(corrSet).size
        val union = origSet.union(corrSet).size

        return if (union > 0) intersection.toDouble() / union else 0.0
    }

    private fun levenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
