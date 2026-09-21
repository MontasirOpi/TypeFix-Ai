package com.typefix.ai.keyboard

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
    private val textReplacer = TextReplacer()
    private val onnxEngine = OnnxModelEngine()
    private lateinit var correctionEngine: CorrectionEngine
    private lateinit var modelManager: ModelManager

    private var suggestionController: SuggestionController? = null
    private var keyboardController: KeyboardController? = null

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

        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        if (textContextReader.isSecureField(info)) {
            suggestionController?.setLoading(false)
            suggestionController?.setStatus(getString(R.string.status_password_safe))
        } else {
            suggestionController?.setLoading(false)
            suggestionController?.setStatus(getString(R.string.status_ready))
        }
    }

    private fun handleCorrectionAction(action: CorrectionAction) {
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

        suggestionController?.setLoading(true, "Processing...")

        serviceScope.launch {
            val result = withContext(Dispatchers.Default) {
                correctionEngine.process(context.text, action)
            }

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
            val replaced = textReplacer.replaceText(
                inputConnection = currentInputConnection,
                context = context,
                newText = result.correctedText
            )

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
        }
    }

    private fun showImePicker() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        onnxEngine.unloadModel()
        super.onDestroy()
    }
}
