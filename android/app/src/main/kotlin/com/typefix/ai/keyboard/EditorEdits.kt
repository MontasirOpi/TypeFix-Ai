package com.typefix.ai.keyboard

import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection

/** Refuses Undo when the editor contents or cursor differ from our last edit. */
class EditorEdits {
    private var undo: UndoEdit? = null
    val canUndo get() = undo != null
    fun clear() { undo = null }

    fun snapshot(ic: InputConnection?): EditSnapshot? = try {
        val value = ic?.getExtractedText(ExtractedTextRequest().apply {
            hintMaxChars = 20000
            hintMaxLines = 100
        }, 0)
        val text = value?.text?.toString()
        if (value == null || text == null || text.length > 20000 || value.partialStartOffset != -1) null
        else EditSnapshot(text, value.selectionStart, value.selectionEnd, value.startOffset)
    } catch (_: RuntimeException) { null }

    fun replace(ic: InputConnection, original: EditSnapshot, from: Int, to: Int, text: String): Boolean {
        clear()
        if (snapshot(ic) != original) return false
        val edit = original.replacing(from, to, text) ?: return false
        return try {
            ic.beginBatchEdit()
            if (!ic.setSelection(original.offset + from, original.offset + to)) return false
            if (!ic.commitText(text, 1)) return false
            undo = edit
            true
        } catch (_: RuntimeException) { false }
        finally { try { ic.endBatchEdit() } catch (_: RuntimeException) { } }
    }

    fun undo(ic: InputConnection): Boolean {
        val edit = undo ?: return false
        clear()
        if (!edit.matches(snapshot(ic))) return false
        return try {
            ic.beginBatchEdit()
            val absolute = edit.after.offset + edit.from
            if (!ic.setSelection(absolute, absolute + edit.inserted.length)) return false
            if (!ic.commitText(edit.removed, 1)) return false
            ic.setSelection(edit.before.offset + edit.before.start, edit.before.offset + edit.before.end)
        } catch (_: RuntimeException) { false }
        finally { try { ic.endBatchEdit() } catch (_: RuntimeException) { } }
    }
}
