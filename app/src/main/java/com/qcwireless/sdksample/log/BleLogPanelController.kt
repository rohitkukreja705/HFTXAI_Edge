package com.qcwireless.sdksample.log

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.qcwireless.sdksample.R

class BleLogPanelController(
    private val activity: Activity,
    private val panelVisibleByDefault: Boolean,
    private val floatingVisibleByDefault: Boolean
) {

    private val panelView: View? by lazy { activity.findViewById(R.id.ll_ble_log_panel) }
    private val draggableView: View? by lazy { activity.findViewById(R.id.draggable_ble_log) }
    private val toggleButton: ImageView? by lazy { activity.findViewById(R.id.iv_ble_log_toggle) }
    private val clearButton: ImageView? by lazy { activity.findViewById(R.id.iv_ble_log_clear) }
    private val collectToggleButton: ImageView? by lazy { activity.findViewById(R.id.iv_ble_log_collect_toggle) }
    private val closeButton: ImageView? by lazy { activity.findViewById(R.id.iv_ble_log_close) }
    private val logTextView: TextView? by lazy { activity.findViewById(R.id.tv_ble_log_message) }
    private val logScrollView: NestedScrollView? by lazy { activity.findViewById(R.id.nsv_ble_log_message) }

    private val isReady: Boolean
        get() = panelView != null &&
            draggableView != null &&
            toggleButton != null &&
            clearButton != null &&
            collectToggleButton != null &&
            closeButton != null &&
            logTextView != null &&
            logScrollView != null

    private var isBound = false

    private val logListener = object : BluetoothLogManager.LogListener {
        override fun onLogTextChanged(content: String) {
            logTextView?.text = content
            scrollToBottom()
        }
    }

    fun bind() {
        if (isBound || !isReady) {
            return
        }
        isBound = true

        panelView?.visibility = if (panelVisibleByDefault) View.VISIBLE else View.GONE
        draggableView?.visibility = if (floatingVisibleByDefault) View.VISIBLE else View.GONE
        toggleButton?.setImageResource(R.mipmap.ic_log_show)
        refreshCollectState()
        if (panelVisibleByDefault) {
            scrollToBottom()
        }

        toggleButton?.setOnClickListener {
            panelView?.let { panel ->
                val show = panel.visibility != View.VISIBLE
                panel.visibility = if (show) View.VISIBLE else View.GONE
                if (show) {
                    scrollToBottom()
                }
            }
        }
        closeButton?.setOnClickListener {
            panelView?.visibility = View.GONE
        }
        clearButton?.setOnClickListener {
            BluetoothLogManager.clear()
        }
        collectToggleButton?.setOnClickListener {
            BluetoothLogManager.isCollecting = !BluetoothLogManager.isCollecting
            refreshCollectState()
        }
    }

    fun onResume() {
        if (!isReady) {
            return
        }
        BluetoothLogManager.addListener(logListener)
        refreshCollectState()
        if (panelView?.visibility == View.VISIBLE) {
            scrollToBottom()
        }
    }

    fun onPause() {
        BluetoothLogManager.removeListener(logListener)
    }

    fun onDestroy() {
        onPause()
    }

    private fun refreshCollectState() {
        val collecting = BluetoothLogManager.isCollecting
        val icon = if (collecting) R.mipmap.ic_log_stop else R.mipmap.ic_log_start
        collectToggleButton?.setImageResource(icon)
        collectToggleButton?.alpha = 1f
    }

    private fun scrollToBottom() {
        logScrollView?.post {
            logScrollView?.fullScroll(View.FOCUS_DOWN)
            logScrollView?.post {
                logScrollView?.fullScroll(View.FOCUS_DOWN)
            }
        }
    }
}