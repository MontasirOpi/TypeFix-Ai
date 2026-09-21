package com.typefix.ai.keyboard

import android.view.inputmethod.InputConnection

/**
 * Handles safe replacement of text within the target application.
 */
class TextReplacer {

    /**
     * Atomically replaces the text defined in TextContext with newText.
     * Returns true if replacement succeeded, false otherwise.
     */
    fun replaceText(
        inputConnection: InputConnection?,
        context: TextContext,
        newText: String
    ): Boolean {
        if (inputConnection == null || !context.isValid || context.isSecureField) {
            return false
        }

        // Failsafe: Never replace non-empty text with an empty string
        if (context.text.isNotBlank() && newText.isBlank()) {
            return false
        }

        // Failsafe: If text is identical, no replacement needed
        if (context.text == newText) {
            return true
        }

        return try {
            inputConnection.beginBatchEdit()

            if (context.isSelected) {
                // For selected text, commitText naturally replaces the active selection
                inputConnection.commitText(newText, 1)
            } else {
                // Delete surrounding text matching the segment read earlier
                if (context.prefixCharsToDelete > 0 || context.suffixCharsToDelete > 0) {
                    inputConnection.deleteSurroundingText(
                        context.prefixCharsToDelete,
                        context.suffixCharsToDelete
                    )
                }
                inputConnection.commitText(newText, 1)
            }

            inputConnection.endBatchEdit()
            true
        } catch (e: Exception) {
            try {
                inputConnection.endBatchEdit()
            } catch (_: Exception) { }
            false
        }
    }
}
