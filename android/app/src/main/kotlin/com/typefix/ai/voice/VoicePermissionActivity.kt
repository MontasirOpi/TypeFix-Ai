package com.typefix.ai.voice

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.typefix.ai.R

/** Runtime permission requests require a visible Activity, not an accessibility service. */
class VoicePermissionActivity : Activity() {
    private var requesting = false
    private val notificationRequest get() = intent.getBooleanExtra("notifications", false)
    private val requestedPermission get() = if (notificationRequest) Manifest.permission.POST_NOTIFICATIONS else Manifest.permission.RECORD_AUDIO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (notificationRequest) {
            if (PermissionStatus.notifications(this)) { finish(); return }
            if (Build.VERSION.SDK_INT < 33 || checkSelfPermission(requestedPermission) == PackageManager.PERMISSION_GRANTED) {
                showSettings(); return
            }
        } else if (checkSelfPermission(requestedPermission) == PackageManager.PERMISSION_GRANTED) {
            finish(); return
        }
        requesting = savedInstanceState?.getBoolean("requesting") ?: false
        if (requesting) return
        val previouslyRequested = getSharedPreferences("permission_requests", MODE_PRIVATE).getBoolean(requestedPermission, false)
        if (previouslyRequested && !shouldShowRequestPermissionRationale(requestedPermission)) {
            showSettings(); return
        }
        AlertDialog.Builder(this)
            .setTitle(if (notificationRequest) R.string.notification_permission_title else R.string.voice_permission_title)
            .setMessage(if (notificationRequest) R.string.notification_permission_explanation else R.string.voice_permission_explanation)
            .setPositiveButton(if (notificationRequest) R.string.notification_allow else R.string.voice_allow) { _, _ ->
                requesting = true
                getSharedPreferences("permission_requests", MODE_PRIVATE).edit().putBoolean(requestedPermission, true).apply()
                requestPermissions(arrayOf(requestedPermission), 1)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
            .setOnCancelListener { finish() }.show()
    }
    private fun showSettings() {
        AlertDialog.Builder(this)
            .setTitle(if (notificationRequest) R.string.notification_permission_title else R.string.voice_permission_title)
            .setMessage(if (notificationRequest) R.string.notification_denied else R.string.microphone_denied)
            .setPositiveButton(R.string.open_app_settings) { _, _ ->
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
                finish()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
            .setOnCancelListener { finish() }.show()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("requesting", requesting)
        super.onSaveInstanceState(outState)
    }
    override fun onRequestPermissionsResult(code: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(code, permissions, results)
        requesting = false
        if (results.firstOrNull() == PackageManager.PERMISSION_GRANTED) finish() else showSettings()
    }
}
