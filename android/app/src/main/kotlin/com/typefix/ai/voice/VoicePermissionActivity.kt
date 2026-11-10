package com.typefix.ai.voice

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import com.typefix.ai.R

/** Runtime permissions must be requested from an Activity, not an IME service. */
class VoicePermissionActivity : Activity() {
    private var requesting = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            finish()
            return
        }
        requesting = savedInstanceState?.getBoolean("requesting") ?: false
        if (requesting) return
        AlertDialog.Builder(this)
            .setTitle(R.string.voice_permission_title)
            .setMessage(R.string.voice_permission_explanation)
            .setPositiveButton(R.string.voice_allow) { _, _ ->
                requesting = true
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("requesting", requesting)
        super.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(code: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(code, permissions, results)
        finish()
    }
}
