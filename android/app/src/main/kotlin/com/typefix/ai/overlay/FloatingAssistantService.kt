package com.typefix.ai.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.typefix.ai.MainActivity
import com.typefix.ai.R
import com.typefix.ai.ai.ModelManager
import com.typefix.ai.ai.OnnxModelEngine
import com.typefix.ai.correction.CorrectionAction
import com.typefix.ai.correction.CorrectionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Foreground Service hosting the floating screen assistant widget.
 * Allows users to keep their favorite keyboard (Gboard, etc.) and trigger TypeFix with 1 tap.
 */
class FloatingAssistantService : Service() {

    companion object {
        const val CHANNEL_ID = "typefix_floating_assistant"
        const val NOTIFICATION_ID = 1001

        var isRunning = false
            private set
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var params: WindowManager.LayoutParams

    private val onnxEngine = OnnxModelEngine()
    private lateinit var correctionEngine: CorrectionEngine

    // UI elements inside floating view
    private var layoutCollapsed: View? = null
    private var layoutExpanded: View? = null
    private var tvLabel: TextView? = null
    private var progress: ProgressBar? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        startAsForeground()

        val modelManager = ModelManager(applicationContext)
        correctionEngine = CorrectionEngine(localModelEngine = onnxEngine)

        // Preload model in background
        serviceScope.launch(Dispatchers.IO) {
            val modelFile = modelManager.getModelFile("en_grammar_v1")
            if (modelFile != null && modelFile.exists()) {
                onnxEngine.loadModel(modelFile.absolutePath)
            }
        }

        setupFloatingView()
    }

    private fun startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.floating_service_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.floating_service_desc)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.floating_service_title))
                .setContentText(getString(R.string.floating_service_desc))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle(getString(R.string.floating_service_title))
                .setContentText(getString(R.string.floating_service_desc))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        }

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun setupFloatingView() {
        if (!Settings.canDrawOverlays(this)) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_toggle_view, null)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 350
        }

        val view = floatingView ?: return
        layoutCollapsed = view.findViewById(R.id.layout_collapsed)
        layoutExpanded = view.findViewById(R.id.layout_expanded)
        tvLabel = view.findViewById(R.id.tv_float_label)
        progress = view.findViewById(R.id.float_progress)

        // Setup touch & drag listener
        setupTouchListener(view)

        // Setup expanded action clicks
        view.findViewById<TextView>(R.id.btn_float_expand_arrow).setOnClickListener {
            toggleExpanded(true)
        }
        view.findViewById<TextView>(R.id.btn_float_collapse).setOnClickListener {
            toggleExpanded(false)
        }
        view.findViewById<TextView>(R.id.btn_float_action_fix).setOnClickListener {
            toggleExpanded(false)
            triggerCorrection(CorrectionAction.FIX)
        }
        view.findViewById<TextView>(R.id.btn_float_action_improve).setOnClickListener {
            toggleExpanded(false)
            triggerCorrection(CorrectionAction.IMPROVE)
        }
        view.findViewById<TextView>(R.id.btn_float_action_pro).setOnClickListener {
            toggleExpanded(false)
            triggerCorrection(CorrectionAction.PROFESSIONAL)
        }
        view.findViewById<TextView>(R.id.btn_float_action_friendly).setOnClickListener {
            toggleExpanded(false)
            triggerCorrection(CorrectionAction.FRIENDLY)
        }

        windowManager.addView(view, params)
    }

    private fun toggleExpanded(expand: Boolean) {
        layoutCollapsed?.visibility = if (expand) View.GONE else View.VISIBLE
        layoutExpanded?.visibility = if (expand) View.VISIBLE else View.GONE
    }

    private fun setupTouchListener(view: View) {
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                        isDragging = true
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(view, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // User performed a tap
                        triggerCorrection(CorrectionAction.FIX)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun triggerCorrection(action: CorrectionAction) {
        val accessService = TypeFixAccessibilityService.instance

        // 1. Try reading directly from the active focused editor via Accessibility
        var textToFix = accessService?.readActiveText()

        // 2. If Accessibility didn't find active text, check clipboard as secondary
        if (textToFix.isNullOrBlank()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val item = clipboard?.primaryClip?.getItemAt(0)
            textToFix = item?.text?.toString()
        }

        if (textToFix.isNullOrBlank()) {
            showTransientFeedback("No text found")
            return
        }

        // Show loading state on floating pill
        setLoading(true)

        serviceScope.launch {
            val result = withContext(Dispatchers.Default) {
                correctionEngine.process(textToFix, action)
            }

            setLoading(false)

            if (!result.isSuccess) {
                showTransientFeedback(result.message ?: "Cannot fix safely")
                return@launch
            }

            if (result.diff.isIdentical) {
                showTransientFeedback("Already correct!")
                return@launch
            }

            // Replace text via AccessibilityService directly in the target app
            var replaced = false
            if (accessService != null && accessService.replaceActiveText(result.correctedText)) {
                replaced = true
            } else {
                // Fallback: copy to clipboard
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("TypeFix", result.correctedText)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(applicationContext, "✨ Corrected! Paste anywhere.", Toast.LENGTH_SHORT).show()
                replaced = true
            }

            if (replaced) {
                showTransientFeedback("✨ Replaced!")
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progress?.visibility = if (loading) View.VISIBLE else View.GONE
        tvLabel?.text = if (loading) "Fixing..." else "✨ Fix"
    }

    private fun showTransientFeedback(text: String) {
        tvLabel?.text = text
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            tvLabel?.text = "✨ Fix"
        }, 2200)
    }

    override fun onDestroy() {
        isRunning = false
        serviceScope.cancel()
        onnxEngine.unloadModel()
        floatingView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) { }
        }
        super.onDestroy()
    }
}
