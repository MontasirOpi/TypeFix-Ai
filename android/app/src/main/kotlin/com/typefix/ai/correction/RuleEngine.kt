package com.typefix.ai.correction

import java.util.Locale

/**
 * Fast, deterministic correction engine running in < 50ms without AI inference.
 */
class RuleEngine {

    companion object {
        // High-frequency typos and misspellings
        val TYPO_MAP: Map<String, String> = mapOf(
            "teh" to "the",
            "recieve" to "receive",
            "definately" to "definitely",
            "seperate" to "separate",
            "occured" to "occurred",
            "untill" to "until",
            "tomorow" to "tomorrow",
            "tommorow" to "tomorrow",
            "beleive" to "believe",
            "wierd" to "weird",
            "calender" to "calendar",
            "accomodate" to "accommodate",
            "goverment" to "government",
            "knowlege" to "knowledge",
            "neccessary" to "necessary",
            "truely" to "truly",
            "goign" to "going",
            "thier" to "their",
            "wich" to "which",
            "alot" to "a lot",
            "noone" to "no one",
            "woudl" to "would",
            "coudl" to "could",
            "shoudl" to "should",
            "becuase" to "because",
            "bcause" to "because",
            "peice" to "piece",
            "freind" to "friend",
            "arguement" to "argument",
            "colleague" to "colleague",
            "concious" to "conscious",
            "embarass" to "embarrass",
            "foriegn" to "foreign",
            "happend" to "happened",
            "independant" to "independent",
            "mispell" to "misspell",
            "noticable" to "noticeable",
            "priviledge" to "privilege",
            "reccomend" to "recommend",
            "succesful" to "successful",
            "suprise" to "surprise",
            "writting" to "writing"
        )

        // Contraction normalizations
        val CONTRACTIONS_MAP: Map<String, String> = mapOf(
            "dont" to "don't",
            "cant" to "can't",
            "wont" to "won't",
            "didnt" to "didn't",
            "isnt" to "isn't",
            "arent" to "aren't",
            "wasnt" to "wasn't",
            "werent" to "weren't",
            "couldnt" to "couldn't",
            "shouldnt" to "shouldn't",
            "wouldnt" to "wouldn't",
            "hasnt" to "hasn't",
            "havent" to "haven't",
            "hadnt" to "hadn't",
            "im" to "I'm",
            "ive" to "I've",
            "id" to "I'd",
            "youre" to "you're",
            "theyre" to "they're",
            "weve" to "we've",
            "youve" to "you've",
            "thats" to "that's",
            "whats" to "what's",
            "wheres" to "where's",
            "hows" to "how's",
            "theres" to "there's",
            "itll" to "it'll",
            "youll" to "you'll",
            "theyll" to "they'll"
        )

        // Proper nouns and days/months to always capitalize
        val CAPITALIZE_WORDS: Set<String> = setOf(
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
            "january", "february", "march", "april", "may", "june", "july",
            "august", "september", "october", "november", "december",
            "english", "spanish", "french", "german", "chinese", "japanese", "bengali"
        )
    }

    /**
     * Executes rule-based correction on the given text.
     */
    fun correct(text: String): String {
        if (text.isBlank()) return text

        // 1. Normalize punctuation spacing (e.g., 'hello , world' -> 'hello, world')
        var result = fixPunctuationSpacing(text)

        // 2. Tokenize into words while preserving delimiters
        result = fixWordsAndContractions(result)

        // 3. Fix isolated lowercase pronoun 'i' -> 'I'
        result = fixPronounI(result)

        // 4. Ensure proper sentence starting capitalization
        result = fixSentenceCapitalization(result)

        return result
    }

    private fun fixPunctuationSpacing(input: String): String {
        var str = input
        // Remove space before punctuation: "word , " -> "word, "
        str = str.replace(Regex("\\s+([,.:;?!])"), "$1")
        // Ensure space after comma/semicolon/colon if followed directly by a letter (avoiding URLs and numbers)
        str = str.replace(Regex("([,;:])([a-zA-Z])"), "$1 $2")
        // Collapse multiple spaces into single space
        str = str.replace(Regex("[ \\t]+"), " ")
        return str
    }

    private fun fixWordsAndContractions(input: String): String {
        // Regex matches word tokens (\b\w+\b)
        return Regex("\\b[a-zA-Z']+\\b").replace(input) { matchResult ->
            val originalWord = matchResult.value
            val lower = originalWord.lowercase(Locale.ROOT)

            // Check typos
            val typoReplacement = TYPO_MAP[lower]
            if (typoReplacement != null) {
                return@replace applyCasing(originalWord, typoReplacement)
            }

            // Check contractions
            val contractionReplacement = CONTRACTIONS_MAP[lower]
            if (contractionReplacement != null) {
                return@replace applyCasing(originalWord, contractionReplacement)
            }

            // Check proper capitalization (days, months, languages)
            if (CAPITALIZE_WORDS.contains(lower)) {
                return@replace lower.replaceFirstChar { it.titlecase(Locale.ROOT) }
            }

            originalWord
        }
    }

    private fun fixPronounI(input: String): String {
        // Standalone 'i' surrounded by word boundaries or punctuation
        return Regex("(\\b)i(\\b)").replace(input, "$1I$2")
    }

    private fun fixSentenceCapitalization(input: String): String {
        // If it's just a single word without spaces, preserve original case
        if (!input.contains(" ") && !input.contains("\n")) {
            return input
        }

        val chars = input.toCharArray()
        var capitalizeNext = true

        for (i in chars.indices) {
            val c = chars[i]
            if (capitalizeNext && c.isLetter()) {
                chars[i] = c.uppercaseChar()
                capitalizeNext = false
            } else if (c == '.' || c == '!' || c == '?') {
                capitalizeNext = true
            } else if (c == '\n') {
                capitalizeNext = true
            }
        }
        return String(chars)
    }

    private fun applyCasing(original: String, replacement: String): String {
        if (original.isEmpty()) return replacement
        // ALL CAPS
        if (original.length > 1 && original.all { !it.isLetter() || it.isUpperCase() }) {
            return replacement.uppercase(Locale.ROOT)
        }
        // Title Case (first char uppercase)
        if (original[0].isUpperCase()) {
            return replacement.replaceFirstChar { it.titlecase(Locale.ROOT) }
        }
        return replacement
    }
}
