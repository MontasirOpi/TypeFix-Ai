package com.typefix.ai.bridge

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.typefix.ai.ai.ModelManager
import com.typefix.ai.overlay.FloatingAssistantService
import com.typefix.ai.overlay.TypeFixAccessibilityService
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * MethodChannel bridge connecting Flutter configuration UI with native Android layer.
 * Strictly used for application settings and model management - NEVER for keystrokes.
 */
class FlutterBridge(private val context: Context) : MethodChannel.MethodCallHandler {

    companion object {
        const val CHANNEL_NAME = "com.typefix.ai/bridge"
    }

    private val modelManager = ModelManager(context)
    private val prefs = context.getSharedPreferences("typefix_prefs", Context.MODE_PRIVATE)

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "isKeyboardEnabled" -> {
                result.success(isKeyboardEnabled())
            }
            "isKeyboardSelected" -> {
                result.success(isKeyboardSelected())
            }
            "openKeyboardSettings" -> {
                try {
                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    result.success(true)
                } catch (e: Exception) {
                    result.error("INTENT_ERROR", e.message, null)
                }
            }
            "openInputMethodPicker" -> {
                try {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showInputMethodPicker()
                    result.success(true)
                } catch (e: Exception) {
                    result.error("PICKER_ERROR", e.message, null)
                }
            }
            // Floating Screen Assistant Methods
            "canDrawOverlays" -> {
                result.success(Settings.canDrawOverlays(context))
            }
            "requestOverlayPermission" -> {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    result.success(true)
                } catch (e: Exception) {
                    result.error("OVERLAY_INTENT_ERROR", e.message, null)
                }
            }
            "isAccessibilityEnabled" -> {
                result.success(TypeFixAccessibilityService.isRunning())
            }
            "openAccessibilitySettings" -> {
                try {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    result.success(true)
                } catch (e: Exception) {
                    result.error("ACCESSIBILITY_INTENT_ERROR", e.message, null)
                }
            }
            "isFloatingAssistantRunning" -> {
                result.success(FloatingAssistantService.isRunning)
            }
            "startFloatingAssistant" -> {
                try {
                    if (Settings.canDrawOverlays(context)) {
                        val intent = Intent(context, FloatingAssistantService::class.java)
                        ContextCompat.startForegroundService(context, intent)
                        result.success(true)
                    } else {
                        result.success(false)
                    }
                } catch (e: Exception) {
                    result.error("START_SERVICE_ERROR", e.message, null)
                }
            }
            "stopFloatingAssistant" -> {
                try {
                    val intent = Intent(context, FloatingAssistantService::class.java)
                    context.stopService(intent)
                    result.success(true)
                } catch (e: Exception) {
                    result.error("STOP_SERVICE_ERROR", e.message, null)
                }
            }
            "getInstalledModels" -> {
                val models = modelManager.getModels().map {
                    mapOf(
                        "id" to it.id,
                        "name" to it.name,
                        "language" to it.language,
                        "version" to it.version,
                        "sizeBytes" to it.sizeBytes,
                        "isInstalled" to it.isInstalled,
                        "description" to it.description
                    )
                }
                result.success(models)
            }
            "deleteModel" -> {
                val modelId = call.argument<String>("modelId") ?: ""
                val deleted = modelManager.deleteModel(modelId)
                result.success(deleted)
            }
            "getPrivacyStatus" -> {
                result.success(
                    mapOf(
                        "isOffline" to true,
                        "noCloudApi" to true,
                        "noKeystrokeLogging" to true,
                        "ignorePasswordFields" to true,
                        "historyDefaultOff" to true
                    )
                )
            }
            "getSettings" -> {
                result.success(
                    mapOf(
                        "historyEnabled" to prefs.getBoolean("history_enabled", false),
                        "ruleEngineEnabled" to prefs.getBoolean("rule_engine_enabled", true),
                        "localAiEnabled" to prefs.getBoolean("local_ai_enabled", true),
                        "activeLanguage" to (prefs.getString("active_language", "en") ?: "en"),
                        "defaultAction" to (prefs.getString("default_action", "FIX") ?: "FIX"),
                        "floatingAssistantEnabled" to prefs.getBoolean("floating_assistant_enabled", false)
                    )
                )
            }
            "updateSettings" -> {
                val key = call.argument<String>("key")
                val value = call.argument<Any>("value")
                if (key != null && value != null) {
                    val editor = prefs.edit()
                    when (value) {
                        is Boolean -> editor.putBoolean(key, value)
                        is String -> editor.putString(key, value)
                        is Int -> editor.putInt(key, value)
                    }
                    editor.apply()
                    result.success(true)
                } else {
                    result.error("INVALID_ARGS", "Key or value was null", null)
                }
            }
            else -> result.notImplemented()
        }
    }

    private fun isKeyboardEnabled(): Boolean {
        val enabledImes = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_INPUT_METHODS
        ) ?: ""
        return enabledImes.contains("com.typefix.ai")
    }

    private fun isKeyboardSelected(): Boolean {
        val defaultIme = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: ""
        return defaultIme.contains("com.typefix.ai")
    }
}
