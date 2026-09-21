package com.typefix.ai.keyboard

import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.typefix.ai.R
import com.typefix.ai.correction.CorrectionAction

/**
 * Manages the suggestion toolbar, action buttons, and status indicator.
 */
class SuggestionController(
    private val rootView: View,
    private val onActionSelected: (CorrectionAction) -> Unit,
    private val onSwitchIme: () -> Unit
) {

    private val btnFix: Button = rootView.findViewById(R.id.btn_fix)
    private val btnImprove: Button = rootView.findViewById(R.id.btn_improve)
    private val btnProfessional: Button = rootView.findViewById(R.id.btn_professional)
    private val btnFriendly: Button = rootView.findViewById(R.id.btn_friendly)
    private val btnShorten: Button = rootView.findViewById(R.id.btn_shorten)
    private val btnExpand: Button = rootView.findViewById(R.id.btn_expand)
    private val btnSwitchIme: Button = rootView.findViewById(R.id.btn_switch_ime)

    private val tvStatus: TextView = rootView.findViewById(R.id.tv_status)
    private val progressLoading: ProgressBar = rootView.findViewById(R.id.progress_loading)

    init {
        btnFix.setOnClickListener { onActionSelected(CorrectionAction.FIX) }
        btnImprove.setOnClickListener { onActionSelected(CorrectionAction.IMPROVE) }
        btnProfessional.setOnClickListener { onActionSelected(CorrectionAction.PROFESSIONAL) }
        btnFriendly.setOnClickListener { onActionSelected(CorrectionAction.FRIENDLY) }
        btnShorten.setOnClickListener { onActionSelected(CorrectionAction.SHORTEN) }
        btnExpand.setOnClickListener { onActionSelected(CorrectionAction.EXPAND) }
        btnSwitchIme.setOnClickListener { onSwitchIme() }
    }

    fun setLoading(isLoading: Boolean, message: String = "Fixing...") {
        progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        tvStatus.text = message
        enableButtons(!isLoading)
    }

    fun setStatus(message: String) {
        progressLoading.visibility = View.GONE
        tvStatus.text = message
    }

    private fun enableButtons(enabled: Boolean) {
        btnFix.isEnabled = enabled
        btnImprove.isEnabled = enabled
        btnProfessional.isEnabled = enabled
        btnFriendly.isEnabled = enabled
        btnShorten.isEnabled = enabled
        btnExpand.isEnabled = enabled
    }
}
