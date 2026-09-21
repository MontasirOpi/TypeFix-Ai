package com.typefix.ai.correction

import java.util.Locale
import kotlin.math.min

/**
 * Lightweight spell checker using Levenshtein distance for single-transposition typos.
 */
class SpellChecker {

    // Common English lexicon for spell verification
    private val dictionary: Set<String> = setOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not",
        "on", "with", "he", "as", "you", "do", "at", "this", "but", "his", "by", "from",
        "they", "we", "say", "her", "she", "or", "an", "will", "my", "one", "all", "would",
        "there", "their", "what", "so", "up", "out", "if", "about", "who", "get", "which",
        "go", "me", "when", "make", "can", "like", "time", "no", "just", "him", "know",
        "take", "people", "into", "year", "your", "good", "some", "could", "them", "see",
        "other", "than", "then", "now", "look", "only", "come", "its", "over", "think",
        "also", "back", "after", "use", "two", "how", "our", "work", "first", "well",
        "way", "even", "new", "want", "because", "any", "these", "give", "day", "most",
        "us", "great", "between", "need", "feel", "high", "place", "little", "world",
        "very", "still", "nation", "hand", "old", "life", "tell", "write", "become",
        "here", "show", "house", "both", "between", "need", "mean", "call", "develop",
        "under", "last", "right", "move", "thing", "general", "school", "never", "same",
        "another", "begin", "while", "number", "part", "turn", "real", "leave", "might",
        "point", "state", "keep", "child", "talk", "start", "find", "large", "plan",
        "office", "tomorrow", "receive", "meeting", "document", "message", "project"
    )

    fun checkWord(word: String): String? {
        val lower = word.lowercase(Locale.ROOT)
        if (dictionary.contains(lower) || lower.length <= 3) {
            return null // Word is already recognized
        }

        // Search for nearest dictionary word within edit distance 1
        var bestMatch: String? = null
        var minDistance = 2

        for (candidate in dictionary) {
            if (candidate.length !in (lower.length - 1)..(lower.length + 1)) continue
            val dist = levenshteinDistance(lower, candidate)
            if (dist < minDistance) {
                minDistance = dist
                bestMatch = candidate
            }
        }

        return if (bestMatch != null) {
            if (word[0].isUpperCase()) bestMatch.replaceFirstChar { it.titlecase(Locale.ROOT) }
            else bestMatch
        } else {
            null
        }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[m][n]
    }
}
