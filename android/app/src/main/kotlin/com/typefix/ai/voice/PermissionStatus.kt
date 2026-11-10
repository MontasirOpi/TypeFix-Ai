package com.typefix.ai.voice

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** Only microphone and (optionally) notifications are runtime permissions. */
object PermissionStatus {
    fun microphone(context: Context) = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    fun notifications(context: Context): Boolean {
        val granted = Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return granted && context.getSystemService(NotificationManager::class.java).areNotificationsEnabled()
    }
}
