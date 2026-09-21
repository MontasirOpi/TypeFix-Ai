package com.typefix.ai.ai

import java.io.File

/**
 * Tokenizer for local sequence models (SentencePiece/BPE compatibility).
 */
class Tokenizer {

    private val vocabMap = mutableMapOf<String, Long>()
    private val idToTokenMap = mutableMapOf<Long, String>()

    val padTokenId: Long = 0L
    val eosTokenId: Long = 1L
    val unkTokenId: Long = 2L

    init {
        // Default standard subword seeds
        vocabMap["<pad>"] = padTokenId
        vocabMap["</s>"] = eosTokenId
        vocabMap["<unk>"] = unkTokenId
        idToTokenMap[padTokenId] = "<pad>"
        idToTokenMap[eosTokenId] = "</s>"
        idToTokenMap[unkTokenId] = "<unk>"
    }

    /**
     * Loads vocabulary from a text or sentencepiece file if provided.
     */
    fun loadVocab(file: File) {
        if (!file.exists()) return
        var id = 3L
        file.forEachLine { line ->
            val token = line.trim()
            if (token.isNotEmpty() && !vocabMap.containsKey(token)) {
                vocabMap[token] = id
                idToTokenMap[id] = token
                id++
            }
        }
    }

    /**
     * Encodes text to LongArray of token IDs, appended with EOS.
     */
    fun encode(text: String, maxLen: Int = 128): LongArray {
        val tokens = mutableListOf<Long>()
        val words = text.split(" ")

        for (word in words) {
            val id = vocabMap[word] ?: vocabMap[" $word"] ?: hashToTokenId(word)
            tokens.add(id)
            if (tokens.size >= maxLen - 1) break
        }

        tokens.add(eosTokenId)
        return tokens.toLongArray()
    }

    /**
     * Decodes LongArray token IDs back to human-readable string.
     */
    fun decode(tokenIds: LongArray): String {
        val sb = StringBuilder()
        for (id in tokenIds) {
            if (id == padTokenId || id == eosTokenId) continue
            val token = idToTokenMap[id]
            if (token != null) {
                if (token.startsWith(" ")) {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append(token.removePrefix(" "))
                } else {
                    sb.append(token)
                }
            }
        }
        return sb.toString().trim()
    }

    private fun hashToTokenId(word: String): Long {
        return (Math.abs(word.hashCode()) % 30000 + 10).toLong()
    }
}
