package com.typefix.ai.keyboard

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * TextContext represents the captured text snapshot from the active editor.
 */
data class TextContext(
    val text: String,
    val isSelected: Boolean,
    val prefixCharsToDelete: Int = 0,
    val suffixCharsToDelete: Int = 0,
    val isSecureField: Boolean = false,
    val isValid: Boolean = true
) {
    companion object {
        val EMPTY = TextContext(text = "", isSelected = false, isValid = false)
        val SECURE = TextContext(text = "", isSelected = false, isSecureField = true, isValid = false)
    }
}

/**
 * Safely reads text context from the active InputConnection without modifying state.
 */
class TextContextReader {

    /**
     * Determines whether the current editor field is sensitive/password-protected.
     */
    fun isSecureField(editorInfo: EditorInfo?): Boolean {
        if (editorInfo == null) return false
        val inputType = editorInfo.inputType
        val textVariation = inputType and InputType.TYPE_MASK_VARIATION

        val isTextPassword = (inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT &&
                (textVariation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                 textVariation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                 textVariation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)

        val isNumberPassword = (inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER &&
                textVariation == InputType.TYPE_NUMBER_VARIATION_PASSWORD

        return isTextPassword || isNumberPassword
    }

    /**
     * Extracts text context to be corrected.
     * Prioritizes selected text; falls back to current sentence or paragraph before cursor.
     */
    fun readContext(inputConnection: InputConnection?, editorInfo: EditorInfo?): TextContext {
        if (inputConnection == null || editorInfo == null) {
            return TextContext.EMPTY
        }

        if (isSecureField(editorInfo)) {
            return TextContext.SECURE
        }

        // 1. Check if user explicitly selected text
        val selected = inputConnection.getSelectedText(0)?.toString()
        if (!selected.isNullOrBlank()) {
            return TextContext(
                text = selected,
                isSelected = true,
                prefixCharsToDelete = 0,
                suffixCharsToDelete = 0,
                isSecureField = false,
                isValid = true
            )
        }

        // 2. Read context before cursor (up to 500 chars)
        val beforeCursor = inputConnection.getTextBeforeCursor(500, 0)?.toString() ?: ""
        if (beforeCursor.isBlank()) {
            return TextContext.EMPTY
        }

        // Extract current sentence/segment before cursor
        // Delimiters for sentences: newline, punctuation followed by whitespace
        val segment = extractActiveSegment(beforeCursor)
        if (segment.isBlank()) {
            return TextContext.EMPTY
        }

        return TextContext(
            text = segment,
            isSelected = false,
            prefixCharsToDelete = segment.length,
            suffixCharsToDelete = 0,
            isSecureField = false,
            isValid = true
        )
    }

    /**
     * Extracts the most relevant sentence or clause ending at the cursor.
     */
    private fun extractActiveSegment(beforeCursor: String): String {
        // If there's a newline, only take text after the last newline
        val lastNewline = beforeCursor.lastIndexOf('\n')
        val line = if (lastNewline >= 0 && lastNewline < beforeCursor.length - 1) {
            beforeCursor.substring(lastNewline + 1)
        } else if (lastNewline == beforeCursor.length - 1) {
            return ""
        } else {
            beforeCursor
        }

        if (line.isBlank()) return ""

        // Find boundary of previous sentence punctuation ('. ', '! ', '? ')
        val sentenceDelimiters = listOf(". ", "! ", "? ", "; ")
        var latestDelimEnd = -1
        for (delim in sentenceDelimiters) {
            val idx = line.lastIndexOf(delim)
            if (idx >= 0) {
                val end = idx + delim.length
                if (end > latestDelimEnd) {
                    latestDelimEnd = end
                }
            }
        }

        return if (latestDelimEnd >= 0 && latestDelimEnd < line.length) {
            line.substring(latestDelimEnd)
        } else {
            line
        }
    }
}
