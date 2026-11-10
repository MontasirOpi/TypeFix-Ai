package com.typefix.ai

import android.Manifest
import android.app.NotificationManager
import com.typefix.ai.voice.PermissionStatus
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class PermissionStatusTest {
    @Test fun microphoneStatusTracksGrantAndRevocation() {
        val app = RuntimeEnvironment.getApplication()
        shadowOf(app).denyPermissions(Manifest.permission.RECORD_AUDIO)
        assertFalse(PermissionStatus.microphone(app))
        shadowOf(app).grantPermissions(Manifest.permission.RECORD_AUDIO)
        assertTrue(PermissionStatus.microphone(app))
        shadowOf(app).denyPermissions(Manifest.permission.RECORD_AUDIO)
        assertFalse(PermissionStatus.microphone(app))
    }
    @Test fun notificationGrantDoesNotGrantMicrophone() {
        val app = RuntimeEnvironment.getApplication()
        shadowOf(app).denyPermissions(Manifest.permission.RECORD_AUDIO)
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        shadowOf(app.getSystemService(NotificationManager::class.java)).setNotificationsEnabled(true)
        assertTrue(PermissionStatus.notifications(app))
        assertFalse(PermissionStatus.microphone(app))
    }
    @Test fun notificationDenialIsReportedSeparately() {
        val app = RuntimeEnvironment.getApplication()
        shadowOf(app).grantPermissions(Manifest.permission.RECORD_AUDIO)
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        shadowOf(app.getSystemService(NotificationManager::class.java)).setNotificationsEnabled(true)
        assertFalse(PermissionStatus.notifications(app))
        assertTrue(PermissionStatus.microphone(app))
    }
    @Test @Config(sdk = [32]) fun olderAndroidUsesNotificationSettingWithoutRuntimePermission() {
        val app = RuntimeEnvironment.getApplication()
        val manager = shadowOf(app.getSystemService(NotificationManager::class.java))
        manager.setNotificationsEnabled(true)
        assertTrue(PermissionStatus.notifications(app))
        manager.setNotificationsEnabled(false)
        assertFalse(PermissionStatus.notifications(app))
    }
}
