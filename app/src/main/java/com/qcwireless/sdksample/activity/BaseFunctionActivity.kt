package com.qcwireless.sdksample.activity

import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.BaseReqCmd
import com.oudmon.ble.base.communication.req.DeviceSupportReq
import com.oudmon.ble.base.communication.req.SetANCSReq
import com.oudmon.ble.base.communication.req.SetTimeReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.DeviceSupportFunctionRsp
import com.oudmon.ble.base.communication.rsp.SetTimeRsp
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.cache.DeviceFunctionSupport
import com.qcwireless.sdksample.cache.SetTime
import com.qcwireless.sdksample.ext.dp
import com.qcwireless.sdksample.ext.showToast
import com.qcwireless.sdksample.log.BluetoothLogManager
import com.qcwireless.sdksample.view.FunctionItemView

open class BaseFunctionActivity : BaseActivity() {
    protected val commandHandle: CommandHandle = CommandHandle.getInstance()
    protected val deviceSupport: DeviceFunctionSupport = DeviceFunctionSupport.instance
    protected val setTimeCache: SetTime = SetTime.instance
    private val gson = Gson()

    @BleIsConnected
    protected fun refreshSupportCache() {
        commandHandle.executeReqCmd(
            DeviceSupportReq.getReadInstance(),
            ICommandResponse<DeviceSupportFunctionRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    deviceSupport.updateFrom(rsp)
                }
            }
        )

        commandHandle.executeReqCmd(
            SetTimeReq(0),
            ICommandResponse<SetTimeRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    setTimeCache.updateFrom(rsp)
                }
            }
        )
    }

    protected fun ensureSupported(supported: Boolean): Boolean {
        if (!supported) {
            getString(R.string.qc_text_0092).showToast()
            return false
        }
        return true
    }

     open fun refreshCachesIfConnected() {
        if (BleOperateManager.getInstance().isConnected) {
            refreshSupportCache()
        }
    }

    protected fun bindSettingAction(
        view: FunctionItemView,
        logTag: String,
        actionName: String,
        requestName: String,
        defaultArgs: String,
        supportCheck: (() -> Boolean)? = null,
        requestProvider: () -> BaseReqCmd
    ) {
        view.setOnClickListener {
            if (supportCheck != null && !supportCheck()) {
                return@setOnClickListener
            }
            executeSettingAction(logTag, actionName, requestName, defaultArgs, requestProvider())
        }
    }

    @BleIsConnected
    protected fun executeSettingAction(
        logTag: String,
        actionName: String,
        requestName: String,
        defaultArgs: String,
        request: BaseReqCmd
    ) {
        XLog.tag(logTag).d("action=$actionName request=$requestName defaultArgs=$defaultArgs")
        commandHandle.executeReqCmd(
            request,
            ICommandResponse<BaseRspCmd> { rsp ->
                val success = rsp.status == BaseRspCmd.RESULT_OK
                XLog.tag(logTag).d(
                    "action=$actionName request=$requestName defaultArgs=$defaultArgs success=$success status=${rsp.status} response=${gson.toJson(rsp)}"
                )
                if (success) {
                    "$actionName success".showToast()
                } else {
                    "$actionName fail".showToast()
                }
            }
        )
    }

     open fun createSetAncsReq(): SetANCSReq {
        return SetANCSReq().apply {
            setCall(true)
            setSms(true)
            setQq(true)
            setWechat(true)
        }
    }

    protected fun ensureNotificationSupported(): Boolean {
        return ensureSupported(deviceSupport.supportNotification || setTimeCache.supportWeChat)
    }

    protected fun ensureMusicSwitchSupported(): Boolean {
        return ensureSupported(deviceSupport.supportRingMusic)
    }

    protected fun ensureHeartRateSettingSupported(): Boolean {
        return ensureSupported(deviceSupport.supportHeartMeasure || setTimeCache.supportManualHeart)
    }

    protected fun ensureBloodOxygenSettingSupported(): Boolean {
        return ensureSupported(setTimeCache.supportBloodOxygen || deviceSupport.supportIntervalBloodOxygen)
    }

    protected fun ensureBloodPressureSettingSupported(): Boolean {
        return ensureSupported(setTimeCache.supportBloodPressure)
    }

    protected fun ensureHrvSettingSupported(): Boolean {
        return ensureSupported(setTimeCache.supportHrv)
    }

    protected fun ensurePressureSettingSupported(): Boolean {
        return ensureSupported(setTimeCache.supportPressure)
    }

    protected fun ensureTemperatureSettingSupported(): Boolean {
        return ensureSupported(
            setTimeCache.supportTemperature ||
                deviceSupport.supportSkinTemperature ||
                deviceSupport.supportIntervalTemperature
        )
    }

    protected fun ensureGestureSupported(): Boolean {
        return ensureSupported(deviceSupport.supportGesture)
    }

    protected fun ensureTouchSupported(): Boolean {
        return ensureSupported(deviceSupport.supportTouch)
    }

    protected fun ensureMuslimSupported(): Boolean {
        return ensureSupported(deviceSupport.supportMuslin)
    }

    protected fun <T> bindHealthQueryActions(
        todayView: FunctionItemView,
        singleDayView: FunctionItemView,
        rangeView: FunctionItemView,
        title: String,
        supportCheck: (() -> Boolean)? = null,
        todayAction: (BleOperateManager.HealthDataCallback<T>) -> Unit,
        singleDayAction: (Int, BleOperateManager.HealthDataCallback<T>) -> Unit,
        rangeAction: (Int, Int, BleOperateManager.HealthDataCallback<List<BleOperateManager.DayIndexedData<T>>>) -> Unit
    ) {
        todayView.setOnClickListener {
            if (!canExecuteHealthQuery(supportCheck)) {
                return@setOnClickListener
            }
            addHealthQueryLog(
                HealthDataQueryLogFormatter.formatRequest(
                    title = title,
                    scope = QUERY_SCOPE_TODAY,
                    params = linkedMapOf("dayIndex" to 0)
                )
            )
            todayAction(createHealthQueryCallback(title, QUERY_SCOPE_TODAY))
        }

        singleDayView.setOnClickListener {
            if (!canExecuteHealthQuery(supportCheck)) {
                return@setOnClickListener
            }
            showSingleNumberDialog(
                dialogTitle = "$title ${getString(R.string.health_query_single_day)}",
                hint = getString(R.string.health_query_day_index_hint),
                defaultValue = "1"
            ) { dayIndex ->
                addHealthQueryLog(
                    HealthDataQueryLogFormatter.formatRequest(
                        title = title,
                        scope = QUERY_SCOPE_SINGLE_DAY,
                        params = linkedMapOf("dayIndex" to dayIndex)
                    )
                )
                singleDayAction(dayIndex, createHealthQueryCallback(title, QUERY_SCOPE_SINGLE_DAY))
            }
        }

        rangeView.setOnClickListener {
            if (!canExecuteHealthQuery(supportCheck)) {
                return@setOnClickListener
            }
            showRangeDialog(
                dialogTitle = "$title ${getString(R.string.health_query_day_range)}"
            ) { startDayIndex, count ->
                addHealthQueryLog(
                    HealthDataQueryLogFormatter.formatRequest(
                        title = title,
                        scope = QUERY_SCOPE_RANGE,
                        params = linkedMapOf(
                            "startDayIndex" to startDayIndex,
                            "count" to count
                        )
                    )
                )
                rangeAction(startDayIndex, count, createHealthQueryCallback(title, QUERY_SCOPE_RANGE))
            }
        }
    }

    private fun canExecuteHealthQuery(supportCheck: (() -> Boolean)?): Boolean {
        return supportCheck?.invoke() ?: true
    }

    private fun addHealthQueryLog(content: String) {
        BluetoothLogManager.addExternalLog(content)
    }

    private fun <T> createHealthQueryCallback(
        title: String,
        scope: String
    ): BleOperateManager.HealthDataCallback<T> {
        return object : BleOperateManager.HealthDataCallback<T> {
            override fun onSuccess(data: T) {
                addHealthQueryLog(
                    HealthDataQueryLogFormatter.formatSuccess(
                        title = title,
                        scope = scope,
                        data = data
                    )
                )
            }

            override fun onError(code: Int, message: String?) {
                addHealthQueryLog(
                    HealthDataQueryLogFormatter.formatError(
                        title = title,
                        scope = scope,
                        code = code,
                        message = message
                    )
                )
            }
        }
    }

    private fun showSingleNumberDialog(
        dialogTitle: String,
        hint: String,
        defaultValue: String,
        onConfirm: (Int) -> Unit
    ) {
        val input = createNumberInput(hint, defaultValue)
        val container = createDialogContainer(input)
        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val value = input.text?.toString()?.trim()?.toIntOrNull()
                if (value == null || value < 0) {
                    getString(R.string.health_query_invalid_number).showToast()
                    return@setPositiveButton
                }
                onConfirm(value)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showRangeDialog(
        dialogTitle: String,
        onConfirm: (Int, Int) -> Unit
    ) {
        val startDayInput = createNumberInput(
            hint = getString(R.string.health_query_start_day_hint),
            defaultValue = "0"
        )
        val countInput = createNumberInput(
            hint = getString(R.string.health_query_count_hint),
            defaultValue = "3"
        )
        val container = createDialogContainer(startDayInput, countInput)
        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val startDayIndex = startDayInput.text?.toString()?.trim()?.toIntOrNull()
                val count = countInput.text?.toString()?.trim()?.toIntOrNull()
                if (startDayIndex == null || startDayIndex < 0 || count == null || count <= 0) {
                    getString(R.string.health_query_invalid_number).showToast()
                    return@setPositiveButton
                }
                onConfirm(startDayIndex, count)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun createNumberInput(
        hint: String,
        defaultValue: String
    ): EditText {
        return EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            this.hint = hint
            setText(defaultValue)
            setSelection(defaultValue.length)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun createDialogContainer(vararg inputs: EditText): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val horizontalPadding = 20.dp
            val verticalPadding = 12.dp
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            inputs.forEach { addView(it) }
        }
    }

    companion object {
        private const val QUERY_SCOPE_TODAY = "Today"
        private const val QUERY_SCOPE_SINGLE_DAY = "Single Day"
        private const val QUERY_SCOPE_RANGE = "Range"
    }
}
