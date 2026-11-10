package com.typefix.ai

import android.view.inputmethod.ExtractedText
import android.view.inputmethod.InputConnection
import com.typefix.ai.keyboard.EditorEdits
import com.typefix.ai.keyboard.EditSnapshot
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Proxy

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class EditorEditsTest {
    private class Editor(var text: String, var start: Int, var end: Int = start, val offset: Int = 0) {
        var allowCommit = true
        var mutations = 0
        val ic = Proxy.newProxyInstance(InputConnection::class.java.classLoader,
            arrayOf(InputConnection::class.java)) { _, method, args ->
            when (method.name) {
                "getExtractedText" -> ExtractedText().also {
                    it.text = text; it.selectionStart = start; it.selectionEnd = end
                    it.startOffset = offset; it.partialStartOffset = -1
                }
                "setSelection" -> {
                    start = (args!![0] as Int) - offset
                    end = (args[1] as Int) - offset
                    mutations++
                    true
                }
                "commitText" -> {
                    if (!allowCommit) false else {
                        val replacement = args!![0].toString()
                        text = text.replaceRange(minOf(start, end), maxOf(start, end), replacement)
                        start = minOf(start, end) + replacement.length; end = start
                        mutations++
                        true
                    }
                }
                "beginBatchEdit", "endBatchEdit" -> true
                else -> null
            }
        } as InputConnection
    }

    @Test fun insertAndUndoAtCursor() {
        val e = Editor("Hello !", 6)
        val edits = EditorEdits()
        assertTrue(edits.replace(e.ic, edits.snapshot(e.ic)!!, 6, 6, "world"))
        assertEquals("Hello world!", e.text)
        assertTrue(edits.canUndo)
        assertTrue(edits.undo(e.ic))
        assertEquals("Hello !", e.text)
        assertEquals(6, e.start)
        assertFalse(edits.canUndo)
    }

    @Test fun selectedTextAndOriginalSelectionAreRestored() {
        val e = Editor("Hello Bob!", 6, 9)
        val edits = EditorEdits()
        assertTrue(edits.replace(e.ic, edits.snapshot(e.ic)!!, 6, 9, "Alice"))
        assertEquals("Hello Alice!", e.text)
        assertTrue(edits.undo(e.ic))
        assertEquals("Hello Bob!", e.text)
        assertEquals(6, e.start); assertEquals(9, e.end)
    }

    @Test fun changedTextRejectsUndoWithoutMutatingEditor() {
        val e = Editor("", 0)
        val edits = EditorEdits()
        edits.replace(e.ic, edits.snapshot(e.ic)!!, 0, 0, "hello")
        e.text = "other" // Same length: text validation must still detect it.
        val count = e.mutations
        assertFalse(edits.undo(e.ic))
        assertEquals(count, e.mutations)
        assertEquals("other", e.text)
    }

    @Test fun movedCursorRejectsUndoWithoutMutatingEditor() {
        val e = Editor("", 0)
        val edits = EditorEdits()
        edits.replace(e.ic, edits.snapshot(e.ic)!!, 0, 0, "hello")
        e.start = 2; e.end = 2
        val count = e.mutations
        assertFalse(edits.undo(e.ic))
        assertEquals(count, e.mutations)
    }

    @Test fun stalePreviewCannotReplaceChangedEditor() {
        val e = Editor("abc", 3)
        val edits = EditorEdits()
        val snapshot = edits.snapshot(e.ic)!!
        e.text = "xyz"
        assertFalse(edits.replace(e.ic, snapshot, 3, 3, "voice"))
        assertEquals(0, e.mutations)
    }

    @Test fun undoWorksWithExtractedWindowOffsetAndUnicode() {
        val e = Editor("Hi 🙂!", 3, 5, offset = 100)
        val edits = EditorEdits()
        assertTrue(edits.replace(e.ic, edits.snapshot(e.ic)!!, 3, 5, "世界"))
        assertEquals("Hi 世界!", e.text)
        assertTrue(edits.undo(e.ic))
        assertEquals("Hi 🙂!", e.text)
        assertEquals(3, e.start); assertEquals(5, e.end)
    }

    @Test fun failedCommitDoesNotOfferUndo() {
        val e = Editor("abc", 3)
        e.allowCommit = false
        val edits = EditorEdits()
        assertFalse(edits.replace(e.ic, edits.snapshot(e.ic)!!, 3, 3, "voice"))
        assertFalse(edits.canUndo)
    }

    @Test fun clearingHistoryPreventsUndo() {
        val e = Editor("", 0)
        val edits = EditorEdits()
        edits.replace(e.ic, edits.snapshot(e.ic)!!, 0, 0, "hello")
        edits.clear()
        assertFalse(edits.undo(e.ic))
        assertEquals("hello", e.text)
    }

    @Test fun invalidRangesAreRejected() {
        assertNull(EditSnapshot("abc", 3, 3).replacing(-1, 3, "x"))
        assertNull(EditSnapshot("abc", 3, 3).replacing(2, 1, "x"))
    }
}
