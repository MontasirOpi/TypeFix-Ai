package com.typefix.ai.overlay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.typefix.ai.correction.CorrectionAction
import com.typefix.ai.correction.CorrectionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles Android system ACTION_PROCESS_TEXT selection menu.
 * Corrects selected text in-place from any app with zero keystroke logging.
 */
class ProcessTextActivity : Activity() {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val correctionEngine = CorrectionEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rawText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        val isReadOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)

        if (rawText.isNullOrBlank()) {
            finish()
            return
        }

        scope.launch {
            val result = withContext(Dispatchers.Default) {
                correctionEngine.process(rawText, CorrectionAction.FIX)
            }

            if (!isReadOnly && result.isSuccess) {
                val output = Intent().apply {
                    putExtra(Intent.EXTRA_PROCESS_TEXT, result.correctedText)
                }
                setResult(RESULT_OK, output)
            } else {
                Toast.makeText(
                    applicationContext,
                    "TypeFix: ${result.correctedText}",
                    Toast.LENGTH_LONG
                ).show()
            }
            finish()
        }
    }
}
