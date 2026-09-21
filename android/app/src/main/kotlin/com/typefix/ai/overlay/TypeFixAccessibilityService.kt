package com.typefix.ai.overlay

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Lightweight, zero-telemetry accessibility service to locate the active focused text input
 * across other applications (WhatsApp, Chrome, Docs) when the user taps the floating toggle.
 */
class TypeFixAccessibilityService : AccessibilityService() {

    companion object {
        var instance: TypeFixAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Only react to focus changes if needed; zero logging of keystrokes
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * Finds the currently focused input view in the foreground window.
     */
    fun getFocusedInputNode(): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        return root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
    }

    /**
     * Reads the current text from the active editor.
     * Guaranteed privacy: Returns null immediately if the field is a password/secure field.
     */
    fun readActiveText(): String? {
        val node = getFocusedInputNode() ?: return null

        // Security check: Ignore password fields
        if (node.isPassword) {
            return null
        }

        return node.text?.toString()
    }

    /**
     * Replaces the text in the active editor with corrected content.
     */
    fun replaceActiveText(newText: String): Boolean {
        val node = getFocusedInputNode() ?: return false

        if (node.isPassword) {
            return false
        }

        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }
}
