package com.typefix.ai.keyboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.TextView
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.view.inputmethod.InputConnection
import com.typefix.ai.voice.OfflineVoiceController
import com.typefix.ai.voice.VoicePermissionActivity
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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

/**
 * Native Android InputMethodService implementing the TypeFix typing and correction pipeline.
 * Fully offline, never streams keystrokes over Flutter or network.
 */
class TypeFixInputMethodService : InputMethodService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val textContextReader = TextContextReader()
    private val editorEdits = EditorEdits()
    private var previewPanel: View? = null
    private var previewText: TextView? = null
    private var insertButton: Button? = null
    private var undoButton: Button? = null
    private var pendingTranscript: String? = null
    private var voiceSnapshot: EditSnapshot? = null
    private val onnxEngine = OnnxModelEngine()
    private lateinit var correctionEngine: CorrectionEngine
    private lateinit var modelManager: ModelManager

    private var suggestionController: SuggestionController? = null
    private var keyboardController: KeyboardController? = null

    private var voiceButton: Button? = null
    private var voiceConnection: InputConnection? = null
    private var correctionInProgress = false
    private var editorGeneration = 0
    private var voiceEditorGeneration = 0
    private val voice by lazy {
        OfflineVoiceController(this,
            onStatus = {
                suggestionController?.setStatus(getString(it))
                if (pendingTranscript == null) previewText?.setText(it)
            },
            onActive = { active ->
                voiceButton?.setText(if (active) R.string.voice_stop else R.string.voice_start)
                suggestionController?.setLoading(active)
                if (active) {
                    previewPanel?.visibility = View.VISIBLE
                    previewText?.setText(R.string.voice_listening)
                    insertButton?.isEnabled = false
                } else if (pendingTranscript == null) {
                    previewPanel?.visibility = View.GONE
                }
                updateUndo()
            },
            onTranscript = { transcript ->
                if (voiceConnection != null && voiceEditorGeneration == editorGeneration &&
                    isInputViewShown && !textContextReader.isSecureField(currentInputEditorInfo)) {
                    pendingTranscript = transcript
                    previewPanel?.visibility = View.VISIBLE
                    previewText?.text = transcript
                    insertButton?.isEnabled = true
                    voiceButton?.isEnabled = false
                    suggestionController?.setLoading(true, getString(R.string.voice_review))
                    suggestionController?.setStatus(getString(R.string.voice_review))
                }
            })
    }

    private fun cancelVoice() {
        voice.cancel()
        voiceConnection = null
        voiceSnapshot = null
        pendingTranscript = null
        previewPanel?.visibility = View.GONE
        voiceButton?.isEnabled = !correctionInProgress && !textContextReader.isSecureField(currentInputEditorInfo)
        updateUndo()
    }

    private fun updateUndo() {
        undoButton?.isEnabled = editorEdits.canUndo && !voice.active && pendingTranscript == null && !correctionInProgress
    }

    private fun insertTranscript() {
        val text = pendingTranscript ?: return
        val ic = voiceConnection ?: return
        val snapshot = voiceSnapshot ?: return
        if (voiceEditorGeneration != editorGeneration || textContextReader.isSecureField(currentInputEditorInfo)) {
            cancelVoice()
            return
        }
        val success = editorEdits.replace(ic, snapshot, minOf(snapshot.start, snapshot.end),
            maxOf(snapshot.start, snapshot.end), text)
        cancelVoice()
        suggestionController?.setStatus(getString(if (success) R.string.voice_inserted else R.string.edit_changed))
        updateUndo()
    }

    private fun toggleVoice() {
        if (correctionInProgress || pendingTranscript != null) return
        if (textContextReader.isSecureField(currentInputEditorInfo)) return
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            suggestionController?.setStatus(getString(R.string.voice_permission))
            startActivity(Intent(this, VoicePermissionActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        }
        if (!voice.active) {
            voiceConnection = currentInputConnection ?: return
            voiceSnapshot = editorEdits.snapshot(voiceConnection)
            if (voiceSnapshot == null) {
                suggestionController?.setStatus(getString(R.string.edit_unsupported))
                voiceConnection = null
                return
            }
            voiceEditorGeneration = editorGeneration
        }
        voice.toggle()
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        editorGeneration++
        editorEdits.clear()
        cancelVoice()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        editorEdits.clear()
        cancelVoice()
        super.onFinishInputView(finishingInput)
    }

    override fun onFinishInput() {
        editorGeneration++
        editorEdits.clear()
        cancelVoice()
        super.onFinishInput()
    }

    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int,
        newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        if ((voice.active || pendingTranscript != null) &&
            (oldSelStart != newSelStart || oldSelEnd != newSelEnd)) {
            cancelVoice()
            suggestionController?.setStatus(getString(R.string.voice_cancelled))
        }
    }

    override fun onCreate() {
        super.onCreate()
        modelManager = ModelManager(applicationContext)
        correctionEngine = CorrectionEngine(localModelEngine = onnxEngine)

        // Try loading model in background if local file exists
        serviceScope.launch(Dispatchers.IO) {
            val modelFile = modelManager.getModelFile("en_grammar_v1")
            if (modelFile != null && modelFile.exists()) {
                onnxEngine.loadModel(modelFile.absolutePath)
            }
        }
    }

    override fun onCreateInputView(): View {
        cancelVoice()
        val view = layoutInflater.inflate(R.layout.keyboard_view, null)

        suggestionController = SuggestionController(
            rootView = view,
            onActionSelected = { action -> handleCorrectionAction(action) },
            onSwitchIme = { showImePicker() }
        )

        keyboardController = KeyboardController(
            context = this,
            rootView = view,
            getInputConnection = { currentInputConnection },
            getEditorInfo = { currentInputEditorInfo }
        )
        keyboardController?.setupKeyboard()

        previewPanel = view.findViewById(R.id.voice_panel)
        previewText = view.findViewById<TextView>(R.id.voice_preview).apply {
            movementMethod = ScrollingMovementMethod()
            isFocusable = false
            isFocusableInTouchMode = false
        }
        insertButton = view.findViewById<Button>(R.id.btn_voice_insert).apply {
            setOnClickListener { insertTranscript() }
        }
        view.findViewById<Button>(R.id.btn_voice_cancel).setOnClickListener {
            cancelVoice()
            suggestionController?.setStatus(getString(R.string.voice_discarded))
        }
        undoButton = view.findViewById<Button>(R.id.btn_undo).apply {
            setOnClickListener {
                val ic = currentInputConnection
                val success = ic != null && !textContextReader.isSecureField(currentInputEditorInfo) && editorEdits.undo(ic)
                updateUndo()
                suggestionController?.setStatus(getString(if (success) R.string.edit_undone else R.string.edit_changed))
            }
        }
        updateUndo()
        voiceButton = view.findViewById<Button>(R.id.btn_voice).apply {
            setOnClickListener { toggleVoice() }
            setOnLongClickListener {
                cancelVoice()
                suggestionController?.setStatus(getString(R.string.status_ready))
                true
            }
        }
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        voiceButton?.isEnabled = !textContextReader.isSecureField(info)
        if (textContextReader.isSecureField(info)) {
            suggestionController?.setLoading(false)
            suggestionController?.setStatus(getString(R.string.status_password_safe))
        } else {
            suggestionController?.setLoading(false)
            suggestionController?.setStatus(getString(R.string.status_ready))
        }
    }

    private fun handleCorrectionAction(action: CorrectionAction) {
        cancelVoice()
        val ic = currentInputConnection
        val info = currentInputEditorInfo

        if (textContextReader.isSecureField(info)) {
            suggestionController?.setStatus(getString(R.string.status_password_safe))
            return
        }

        val context = textContextReader.readContext(ic, info)
        if (!context.isValid || context.text.isBlank()) {
            suggestionController?.setStatus(getString(R.string.status_no_text))
            return
        }

        val original = editorEdits.snapshot(ic)
        if (ic == null || original == null) {
            suggestionController?.setStatus(getString(R.string.edit_unsupported))
            return
        }
        val from = if (context.isSelected) minOf(original.start, original.end) else original.start - context.prefixCharsToDelete
        val to = if (context.isSelected) maxOf(original.start, original.end) else original.end + context.suffixCharsToDelete
        if (from !in 0..original.text.length || to !in from..original.text.length || original.text.substring(from, to) != context.text) {
            suggestionController?.setStatus(getString(R.string.edit_changed))
            return
        }
        suggestionController?.setLoading(true, "Processing...")

        val correctionEditorGeneration = editorGeneration
        correctionInProgress = true
        updateUndo()
        voiceButton?.isEnabled = false
        serviceScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    correctionEngine.process(context.text, action)
                }

                if (correctionEditorGeneration != editorGeneration) return@launch
                suggestionController?.setLoading(false)

                if (!result.isSuccess) {
                    suggestionController?.setStatus(result.message ?: "Cannot safely fix text")
                    return@launch
                }

                if (result.diff.isIdentical) {
                    suggestionController?.setStatus("Text already looks great!")
                    return@launch
                }

                // Atomically replace text in active editor
                val replaced = editorEdits.replace(ic, original, from, to, result.correctedText)
                updateUndo()

                if (replaced) {
                    val actionName = when (action) {
                        CorrectionAction.FIX -> "✨ Fixed"
                        CorrectionAction.IMPROVE -> "Improved"
                        CorrectionAction.PROFESSIONAL -> "Rewritten (Formal)"
                        CorrectionAction.FRIENDLY -> "Rewritten (Friendly)"
                        CorrectionAction.SHORTEN -> "Shortened"
                        CorrectionAction.EXPAND -> "Expanded"
                    }
                    suggestionController?.setStatus("$actionName text safely")
                } else {
                    suggestionController?.setStatus("Failed to update editor")
                }
            } finally {
                correctionInProgress = false
                updateUndo()
                voiceButton?.isEnabled = !textContextReader.isSecureField(currentInputEditorInfo)
            }
        }
    }

    private fun showImePicker() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }

    override fun onDestroy() {
        cancelVoice()
        serviceScope.cancel()
        onnxEngine.unloadModel()
        super.onDestroy()
    }
}
