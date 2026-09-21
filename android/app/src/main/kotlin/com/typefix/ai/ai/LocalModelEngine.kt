package com.typefix.ai.ai

/**
 * Standard abstraction for on-device AI models.
 * Allows ONNX Runtime or future engines to be plugged in without changing keyboard logic.
 */
interface LocalModelEngine {
    fun loadModel(modelPath: String): Boolean
    fun unloadModel()
    fun isLoaded(): Boolean
    suspend fun correctText(input: String): String
    suspend fun rewriteText(input: String, action: String): String
    fun getModelInfo(): ModelMetadata?
}
