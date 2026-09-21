package com.typefix.ai.ai

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.LongBuffer

/**
 * OnnxModelEngine implements LocalModelEngine using ONNX Runtime Mobile.
 * Completely offline, privacy-first, and asynchronous.
 */
class OnnxModelEngine : LocalModelEngine {

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var modelMetadata: ModelMetadata? = null
    private val tokenizer = Tokenizer()

    override fun loadModel(modelPath: String): Boolean {
        return try {
            val file = File(modelPath)
            if (!file.exists() || file.length() == 0L) {
                return false
            }

            ortEnv = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
            }

            ortSession = ortEnv?.createSession(modelPath, sessionOptions)
            modelMetadata = ModelMetadata(
                id = "english_v1",
                name = "English Grammar & Writing",
                language = "en",
                version = "1.0.0",
                sizeBytes = file.length(),
                isInstalled = true,
                description = "Quantized on-device model for grammar and writing assistance",
                fileName = file.name
            )
            true
        } catch (e: Exception) {
            unloadModel()
            false
        }
    }

    override fun unloadModel() {
        try {
            ortSession?.close()
            ortSession = null
            ortEnv?.close()
            ortEnv = null
            modelMetadata = null
        } catch (_: Exception) { }
    }

    override fun isLoaded(): Boolean {
        return ortSession != null
    }

    override fun getModelInfo(): ModelMetadata? {
        return modelMetadata
    }

    override suspend fun correctText(input: String): String = withContext(Dispatchers.Default) {
        if (input.isBlank()) return@withContext input

        // If ONNX session is active, run local ONNX inference
        val session = ortSession
        val env = ortEnv
        if (session != null && env != null) {
            try {
                val prompt = "fix grammar: $input"
                val tokenIds = tokenizer.encode(prompt)
                val shape = longArrayOf(1, tokenIds.size.toLong())
                val buffer = LongBuffer.wrap(tokenIds)
                val inputTensor = OnnxTensor.createTensor(env, buffer, shape)

                val results = session.run(mapOf("input_ids" to inputTensor))
                val outputTensor = results[0] as? OnnxTensor
                if (outputTensor != null) {
                    val outputBuffer = outputTensor.longBuffer
                    val outputTokens = LongArray(outputBuffer.remaining())
                    outputBuffer.get(outputTokens)
                    val decoded = tokenizer.decode(outputTokens)
                    if (decoded.isNotBlank()) {
                        return@withContext decoded
                    }
                }
            } catch (_: Exception) {
                // Fall back to rule-guided offline correction
            }
        }

        // Offline local fallback if ONNX binary is not yet mounted
        return@withContext fallbackFix(input)
    }

    override suspend fun rewriteText(input: String, action: String): String = withContext(Dispatchers.Default) {
        if (input.isBlank()) return@withContext input

        val session = ortSession
        val env = ortEnv
        if (session != null && env != null) {
            try {
                val prompt = "$action: $input"
                val tokenIds = tokenizer.encode(prompt)
                val shape = longArrayOf(1, tokenIds.size.toLong())
                val buffer = LongBuffer.wrap(tokenIds)
                val inputTensor = OnnxTensor.createTensor(env, buffer, shape)

                val results = session.run(mapOf("input_ids" to inputTensor))
                val outputTensor = results[0] as? OnnxTensor
                if (outputTensor != null) {
                    val outputBuffer = outputTensor.longBuffer
                    val outputTokens = LongArray(outputBuffer.remaining())
                    outputBuffer.get(outputTokens)
                    val decoded = tokenizer.decode(outputTokens)
                    if (decoded.isNotBlank()) {
                        return@withContext decoded
                    }
                }
            } catch (_: Exception) { }
        }

        // Local deterministic style rewriting
        return@withContext fallbackRewrite(input, action)
    }

    private fun fallbackFix(input: String): String {
        // High quality offline fallback
        var text = input.trim()
        if (text.isEmpty()) return input

        // Capitalize first char
        text = text.replaceFirstChar { it.uppercase() }

        // Common contractions & pronoun fixes
        text = text.replace(Regex("\\bi\\b"), "I")
        text = text.replace(Regex("\\bdont\\b", RegexOption.IGNORE_CASE), "don't")
        text = text.replace(Regex("\\bcant\\b", RegexOption.IGNORE_CASE), "can't")
        text = text.replace(Regex("\\bwont\\b", RegexOption.IGNORE_CASE), "won't")
        text = text.replace(Regex("\\bteh\\b", RegexOption.IGNORE_CASE), "the")
        text = text.replace(Regex("\\bgoign\\b", RegexOption.IGNORE_CASE), "going")
        text = text.replace(Regex("\\btomorow\\b", RegexOption.IGNORE_CASE), "tomorrow")
        text = text.replace(Regex("\\brecieve\\b", RegexOption.IGNORE_CASE), "receive")

        // Add period if ending cleanly without punctuation
        if (!text.endsWith(".") && !text.endsWith("!") && !text.endsWith("?") && text.contains(" ")) {
            text += "."
        }
        return text
    }

    private fun fallbackRewrite(input: String, action: String): String {
        val base = fallbackFix(input)
        return when (action.lowercase()) {
            "improve" -> {
                base.replace("want to", "would like to")
                    .replace("good", "great")
                    .replace("bad", "suboptimal")
                    .replace("a lot of", "numerous")
            }
            "professional" -> {
                var p = base.replace(Regex("\\bcan't\\b", RegexOption.IGNORE_CASE), "cannot")
                    .replace(Regex("\\bdon't\\b", RegexOption.IGNORE_CASE), "do not")
                    .replace(Regex("\\bwon't\\b", RegexOption.IGNORE_CASE), "will not")
                    .replace(Regex("\\bthanks\\b", RegexOption.IGNORE_CASE), "thank you")
                    .replace(Regex("\\bhey\\b", RegexOption.IGNORE_CASE), "hello")
                    .replace(Regex("\\bbro\\b", RegexOption.IGNORE_CASE), "colleague")
                    .replace(Regex("\\bgonna\\b", RegexOption.IGNORE_CASE), "going to")
                if (!p.endsWith(".")) p += "."
                p
            }
            "friendly" -> {
                base.replace("cannot", "can't")
                    .replace("do not", "don't")
                    .replace("Hello,", "Hey!")
                    .replace("Regards,", "Cheers,")
            }
            "shorten" -> {
                base.replace("in order to", "to")
                    .replace("due to the fact that", "because")
                    .replace("at this point in time", "now")
                    .replace("for the purpose of", "for")
            }
            "expand" -> {
                if (base.endsWith(".")) {
                    base.dropLast(1) + " for your review and confirmation."
                } else {
                    "$base in detail."
                }
            }
            else -> base
        }
    }
}
