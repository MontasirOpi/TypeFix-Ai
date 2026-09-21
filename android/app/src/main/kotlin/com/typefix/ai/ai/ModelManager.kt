package com.typefix.ai.ai

import android.content.Context
import java.io.File
import java.io.FileOutputStream

/**
 * Manages local AI models on disk, installation, status, and lifecycle.
 */
class ModelManager(private val context: Context) {

    private val modelsDir: File = File(context.filesDir, "models").apply {
        if (!exists()) mkdirs()
    }

    private val availableModels = listOf(
        ModelMetadata(
            id = "en_grammar_v1",
            name = "English Grammar & Writing",
            language = "English",
            version = "1.0.0",
            sizeBytes = 28 * 1024 * 1024L, // ~28 MB
            isInstalled = true,
            description = "Quantized on-device model for grammar, punctuation, and style",
            fileName = "english_v1.onnx"
        ),
        ModelMetadata(
            id = "bn_grammar_v1",
            name = "Bangla Writing Assistant",
            language = "Bangla",
            version = "0.9.0",
            sizeBytes = 32 * 1024 * 1024L,
            isInstalled = false,
            description = "Local Bangla grammar correction model (Coming Soon)",
            fileName = "bangla_v1.onnx"
        )
    )

    fun getModels(): List<ModelMetadata> {
        return availableModels.map { model ->
            val file = File(modelsDir, model.fileName ?: "${model.id}.onnx")
            val isInstalled = file.exists() && file.length() > 0 || model.id == "en_grammar_v1"
            val size = if (file.exists()) file.length() else model.sizeBytes
            model.copy(isInstalled = isInstalled, sizeBytes = size)
        }
    }

    fun getModelFile(modelId: String): File? {
        val model = availableModels.find { it.id == modelId } ?: return null
        val file = File(modelsDir, model.fileName ?: "${model.id}.onnx")
        if (!file.exists()) {
            // Check if available in assets
            try {
                context.assets.open("models/${model.fileName}").use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (_: Exception) {
                // Return null if not in assets
                return null
            }
        }
        return file
    }

    fun deleteModel(modelId: String): Boolean {
        val model = availableModels.find { it.id == modelId } ?: return false
        val file = File(modelsDir, model.fileName ?: "${model.id}.onnx")
        return if (file.exists()) file.delete() else true
    }
}
