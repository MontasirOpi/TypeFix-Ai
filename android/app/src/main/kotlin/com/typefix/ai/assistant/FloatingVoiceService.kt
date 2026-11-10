package com.typefix.ai.assistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.pm.ServiceInfo
import android.os.Build
import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.typefix.ai.R
import com.typefix.ai.keyboard.EditSnapshot
import com.typefix.ai.voice.OfflineVoiceController
import com.typefix.ai.voice.VoicePermissionActivity

/** System-managed accessibility overlay: no keyboard replacement or overlay/FGS permission. */
class FloatingVoiceService : AccessibilityService() {
    companion object {
        var instance: FloatingVoiceService? = null
            private set
    }
    private val handler = Handler(Looper.getMainLooper())
    private var panel: LinearLayout? = null
    private var mic: Button? = null
    private var undoButton: Button? = null
    private var label: TextView? = null
    private var positionY = -1
    private var target: AccessibilityNodeInfo? = null
    private var original: EditSnapshot? = null
    private var undoTarget: AccessibilityNodeInfo? = null
    private var undoBefore: EditSnapshot? = null
    private var undoAfter: EditSnapshot? = null
    private var applying = false
    private val wm by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private val voice by lazy {
        OfflineVoiceController(this,
            onStatus = { label?.setText(it) },
            onActive = { active ->
                if (active) startMicrophoneForeground() else stopForeground(STOP_FOREGROUND_REMOVE)
                mic?.setText(if (active) R.string.voice_stop else R.string.voice_start)
                undoButton?.isEnabled = !active && undoAfter != null
            },
            onTranscript = { insert(it) })
    }
    private val refreshWidget = Runnable { refreshWidget() }

    override fun onServiceConnected() {
        instance = this
        refreshWidget()
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || applying) return
        // Events only schedule keyboard/focus checks; event text is never logged or saved.
        handler.removeCallbacks(refreshWidget)
        handler.postDelayed(refreshWidget, 80)
    }
    private fun focusedEditor(): AccessibilityNodeInfo? {
        for (window in windows) {
            if (window.type != AccessibilityWindowInfo.TYPE_APPLICATION) continue
            val node = window.root?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: continue
            if (node.isEditable && node.isFocused && node.isVisibleToUser && !node.isPassword &&
                node.packageName?.toString() != packageName) return node
        }
        return null
    }
    private fun snapshot(node: AccessibilityNodeInfo): EditSnapshot? {
        if (!node.refresh() || node.isPassword || !node.isEditable || !node.isFocused) return null
        val text = if (node.isShowingHintText) "" else node.text?.toString().orEmpty()
        if (text.length > 20000 || node.textSelectionStart !in 0..text.length ||
            node.textSelectionEnd !in 0..text.length) return null
        return EditSnapshot(text, node.textSelectionStart, node.textSelectionEnd)
    }
    private fun sameTarget(node: AccessibilityNodeInfo?, expected: AccessibilityNodeInfo?) =
        node != null && expected != null && node == expected

    private fun refreshWidget() {
        val ime = windows.firstOrNull { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
        val editor = focusedEditor()
        if (ime == null || editor == null) { hide(); return }
        if (voice.active && (!sameTarget(editor, target) || snapshot(editor) != original)) {
            cancel()
            label?.setText(R.string.voice_cancelled)
        }
        if (undoAfter != null && (!sameTarget(editor, undoTarget) || snapshot(editor) != undoAfter)) clearUndo()
        val bounds = Rect()
        ime.getBoundsInScreen(bounds)
        show(bounds.top)
    }
    private fun show(keyboardTop: Int) {
        if (panel == null) {
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(8), dp(4), dp(8), dp(4))
                setBackgroundColor(0xff242936.toInt())
            }
            label = TextView(this).apply {
                setTextColor(0xffe0e2e6.toInt()); textSize = 12f
                text = "TypeFix • offline voice"; maxLines = 2
            }
            container.addView(label)
            val row = LinearLayout(this)
            mic = Button(this).apply { setText(R.string.voice_start); setOnClickListener { speak() } }
            row.addView(mic)
            row.addView(Button(this).apply {
                setText(android.R.string.cancel)
                setOnClickListener { cancel(); label?.setText(R.string.voice_discarded) }
            })
            undoButton = Button(this).apply {
                setText(R.string.action_undo); isEnabled = false; setOnClickListener { undo() }
            }
            row.addView(undoButton)
            container.addView(row)
            panel = container
            positionY = (keyboardTop - dp(104)).coerceAtLeast(dp(30))
            try { wm.addView(container, parameters(positionY)) }
            catch (_: RuntimeException) { panel = null }
        } else {
            val y = (keyboardTop - dp(104)).coerceAtLeast(dp(30))
            if (y != positionY) {
                positionY = y
                try { wm.updateViewLayout(panel, parameters(y)) } catch (_: RuntimeException) { hide() }
            }
        }
    }
    private fun parameters(y: Int) = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.END; x = dp(8); this.y = y }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun startMicrophoneForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("voice", "Voice typing", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 0, Intent().setClassName(packageName, "com.typefix.ai.MainActivity"), PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, "voice")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("TypeFix is listening")
            .setContentText("Tap Finish or Cancel on the floating widget.")
            .setContentIntent(open).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(301, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        else startForeground(301, notification)
    }
    private fun speak() {
        if (voice.active) { voice.toggle(); return }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(this, VoicePermissionActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        }
        val node = focusedEditor() ?: return
        val state = snapshot(node)
        if (state == null || !node.actionList.any { it.id == AccessibilityNodeInfo.ACTION_SET_TEXT }) {
            label?.text = "This field does not support voice insertion."
            return
        }
        target = node
        original = state
        clearUndo()
        voice.toggle()
    }
    private fun setText(node: AccessibilityNodeInfo, state: EditSnapshot): Boolean {
        if (!node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, state.text)
        })) return false
        node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, Bundle().apply {
            putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, state.start)
            putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, state.end)
        })
        return true
    }
    private fun insert(transcript: String) {
        val node = target
        val before = original
        target = null; original = null
        if (node == null || before == null || !sameTarget(focusedEditor(), node) || snapshot(node) != before) {
            label?.setText(R.string.voice_cancelled); return
        }
        val edit = before.replacing(minOf(before.start, before.end), maxOf(before.start, before.end), transcript) ?: return
        applying = true
        try {
            if (setText(node, edit.after)) {
                undoTarget = node; undoBefore = before; undoAfter = edit.after
                undoButton?.isEnabled = true
                label?.text = "Inserted • tap Undo to restore"
            } else label?.text = "This app refused insertion. No clipboard access was used."
        } finally { applying = false }
    }
    private fun undo() {
        val node = undoTarget
        val before = undoBefore
        val expected = undoAfter
        clearUndo()
        if (node == null || before == null || expected == null ||
            !sameTarget(focusedEditor(), node) || snapshot(node) != expected) {
            label?.setText(R.string.edit_changed); return
        }
        applying = true
        try { label?.setText(if (setText(node, before)) R.string.edit_undone else R.string.edit_changed) }
        finally { applying = false }
    }
    private fun clearUndo() {
        undoTarget = null; undoBefore = null; undoAfter = null; undoButton?.isEnabled = false
    }
    private fun cancel() { voice.cancel(); target = null; original = null }
    private fun hide() {
        cancel(); clearUndo()
        panel?.let { try { wm.removeView(it) } catch (_: RuntimeException) { } }
        panel = null; label = null; mic = null; undoButton = null; positionY = -1
    }
    override fun onInterrupt() { hide() }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null); hide(); instance = null; super.onDestroy()
    }
}
