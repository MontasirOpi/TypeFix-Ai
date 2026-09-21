package com.typefix.ai.keyboard

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import com.typefix.ai.R

/**
 * Controller for rendering and handling the on-screen keyboard key deck.
 */
class KeyboardController(
    private val context: Context,
    private val rootView: View,
    private val getInputConnection: () -> InputConnection?,
    private val getEditorInfo: () -> EditorInfo?
) {

    private var isShifted = false
    private var isNumbersMode = false

    private val row1: LinearLayout = rootView.findViewById(R.id.row_1)
    private val row2: LinearLayout = rootView.findViewById(R.id.row_2)
    private val row3: LinearLayout = rootView.findViewById(R.id.row_3)
    private val row4: LinearLayout = rootView.findViewById(R.id.row_4)

    private val letterRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )

    private val numberRows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "_", "&", "-", "+", "(", ")"),
        listOf("*", "\"", "'", ":", ";", "!", "?")
    )

    fun setupKeyboard() {
        renderKeys()
    }

    private fun renderKeys() {
        row1.removeAllViews()
        row2.removeAllViews()
        row3.removeAllViews()
        row4.removeAllViews()

        val activeRows = if (isNumbersMode) numberRows else letterRows

        // Row 1
        for (char in activeRows[0]) {
            val key = createKey(getCharForDisplay(char)) { commitChar(char) }
            val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                marginEnd = dp(3)
            }
            row1.addView(key, params)
        }

        // Row 2
        for (char in activeRows[1]) {
            val key = createKey(getCharForDisplay(char)) { commitChar(char) }
            val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f).apply {
                marginEnd = dp(3)
            }
            row2.addView(key, params)
        }

        // Row 3: Shift / Symbol toggle + letters + Backspace
        val shiftLabel = if (isNumbersMode) "=\\<" else if (isShifted) "⬆" else "⇧"
        val shiftKey = createKey(shiftLabel, weight = 1.4f) {
            if (isNumbersMode) {
                // Toggle extra symbols
            } else {
                isShifted = !isShifted
                renderKeys()
            }
        }
        row3.addView(shiftKey)

        for (char in activeRows[2]) {
            val key = createKey(getCharForDisplay(char), weight = 1f) { commitChar(char) }
            row3.addView(key)
        }

        val backspaceKey = createKey("⌫", weight = 1.4f) {
            handleBackspace()
        }
        row3.addView(backspaceKey)

        // Row 4: Mode switch (?123 / ABC) + Comma + Space + Period + Enter
        val modeLabel = if (isNumbersMode) "ABC" else "?123"
        val modeKey = createKey(modeLabel, weight = 1.5f) {
            isNumbersMode = !isNumbersMode
            isShifted = false
            renderKeys()
        }
        row4.addView(modeKey)

        val commaKey = createKey(",", weight = 1f) { commitChar(",") }
        row4.addView(commaKey)

        val spaceKey = createKey("Space", weight = 4.5f) { commitChar(" ") }
        row4.addView(spaceKey)

        val periodKey = createKey(".", weight = 1f) { commitChar(".") }
        row4.addView(periodKey)

        val enterKey = createKey("↵", weight = 1.5f) { handleEnter() }
        row4.addView(enterKey)
    }

    private fun getCharForDisplay(char: String): String {
        return if (!isNumbersMode && isShifted) char.uppercase() else char
    }

    private fun commitChar(char: String) {
        val toCommit = if (!isNumbersMode && isShifted) {
            char.uppercase()
        } else {
            char
        }
        getInputConnection()?.commitText(toCommit, 1)

        // Auto reset shift after one uppercase letter
        if (isShifted && !isNumbersMode) {
            isShifted = false
            renderKeys()
        }
    }

    private fun handleBackspace() {
        val ic = getInputConnection() ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun handleEnter() {
        val ic = getInputConnection() ?: return
        val editorInfo = getEditorInfo()

        if (editorInfo != null) {
            val action = editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION
            if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                ic.performEditorAction(action)
                return
            }
        }
        ic.commitText("\n", 1)
    }

    private fun createKey(
        label: String,
        weight: Float = 1f,
        onClick: () -> Unit
    ): Button {
        return Button(context).apply {
            text = label
            isAllCaps = false
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundResource(R.drawable.bg_key)
            setPadding(0, 0, 0, 0)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight).apply {
                marginEnd = dp(3)
                marginStart = dp(3)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
