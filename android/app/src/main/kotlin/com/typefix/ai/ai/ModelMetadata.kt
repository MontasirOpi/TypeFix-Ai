package com.typefix.ai.ai

data class ModelMetadata(
    val id: String,
    val name: String,
    val language: String,
    val version: String,
    val sizeBytes: Long,
    val isInstalled: Boolean,
    val description: String,
    val fileName: String? = null
)
