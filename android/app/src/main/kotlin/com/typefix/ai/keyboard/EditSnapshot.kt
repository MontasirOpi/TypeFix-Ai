package com.typefix.ai.keyboard

/** In-memory, single-edit history. Offsets are relative to the extracted window. */
data class EditSnapshot(val text: String, val start: Int, val end: Int, val offset: Int = 0) {
    fun replacing(from: Int, to: Int, replacement: String): UndoEdit? {
        if (from !in 0..text.length || to !in from..text.length ||
            start !in 0..text.length || end !in 0..text.length) return null
        val cursor = from + replacement.length
        return UndoEdit(this, copy(text = text.replaceRange(from, to, replacement),
            start = cursor, end = cursor), from, text.substring(from, to), replacement)
    }
}

data class UndoEdit(val before: EditSnapshot, val after: EditSnapshot, val from: Int,
    val removed: String, val inserted: String) {
    fun matches(current: EditSnapshot?) = current == after
}
