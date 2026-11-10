package com.typefix.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.typefix.ai.R
import java.util.Locale

/** Only uses the explicitly on-device API. Never falls back to a network recognizer. */
class OfflineVoiceController(
    private val context: Context,
    private val onStatus: (Int) -> Unit,
    private val onActive: (Boolean) -> Unit,
    private val onTranscript: (String) -> Unit
) {
    private var recognizer: SpeechRecognizer? = null
    private var generation = 0
    private val handler = Handler(Looper.getMainLooper())
    private val timeout = Runnable { cancel(); onStatus(R.string.voice_timeout) }
    var active = false
        private set
    private var finishing = false

    fun toggle() {
        if (active) {
            if (!finishing) {
                finishing = true
                onStatus(R.string.voice_processing)
                recognizer?.stopListening()
            }
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            !SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
            onStatus(R.string.voice_unavailable)
            return
        }
        val token = ++generation
        try {
            val speech = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            recognizer = speech
            speech.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (token == generation) onStatus(R.string.voice_listening)
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    if (token == generation) {
                        finishing = true
                        onStatus(R.string.voice_processing)
                    }
                }
                override fun onError(error: Int) {
                    if (token != generation) return
                    cancel()
                    onStatus(if (error == SpeechRecognizer.ERROR_NO_MATCH ||
                        error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) R.string.voice_timeout else R.string.voice_error)
                }
                override fun onResults(results: Bundle?) {
                    if (token != generation) return
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()?.trim().orEmpty()
                    cancel()
                    if (text.isEmpty()) onStatus(R.string.voice_timeout) else onTranscript(text)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            active = true
            finishing = false
            onActive(true)
            onStatus(R.string.voice_listening)
            handler.postDelayed(timeout, 60_000)
            speech.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            })
        } catch (_: RuntimeException) {
            cancel()
            onStatus(R.string.voice_error)
        }
    }

    fun cancel() {
        ++generation
        handler.removeCallbacks(timeout)
        val speech = recognizer
        recognizer = null
        active = false
        finishing = false
        try { speech?.cancel() } catch (_: RuntimeException) { }
        try { speech?.destroy() } catch (_: RuntimeException) { }
        onActive(false)
    }
}
