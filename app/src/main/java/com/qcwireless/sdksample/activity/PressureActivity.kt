package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.PressureReq
import com.oudmon.ble.base.communication.req.PressureSettingReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.PressureRsp
import com.oudmon.ble.base.communication.rsp.StartHeartRateRsp
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivityPressureBinding
import com.qcwireless.sdksample.ext.showToast

class PressureActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivityPressureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPressureBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0063)
        binding.fivSettingRead.setOnClickListener { readPressureSetting() }
        binding.fivSettingWrite.setOnClickListener { writePressureSetting() }
        binding.fivDataSync.setOnClickListener { syncPressureData() }
        bindHealthQueryActions<PressureRsp>(
            todayView = binding.fivTodayData,
            singleDayView = binding.fivSingleDayData,
            rangeView = binding.fivRangeData,
            title = getString(R.string.qc_text_0063),
            supportCheck = { isPressureSupported() },
            todayAction = { callback ->
                BleOperateManager.getInstance().getTodayPressure(callback)
            },
            singleDayAction = { dayIndex, callback ->
                BleOperateManager.getInstance().getPressure(dayIndex, callback)
            },
            rangeAction = { startDayIndex, count, callback ->
                BleOperateManager.getInstance().getPressures(startDayIndex, count, callback)
            }
        )
        binding.fivStartMeasure.setOnClickListener { startPressureMeasure() }
        binding.fivStopMeasure.setOnClickListener { stopPressureMeasure() }
        refreshSupportCache()
    }

    private fun isPressureSupported(): Boolean {
        return ensureSupported(setTimeCache.supportPressure)
    }

    @BleIsConnected
    private fun readPressureSetting() {
        if (!ensurePressureSettingSupported()) {
            return
        }
        executeSettingAction(
            LOG_TAG,
            "Get Pressure Setting",
            "PressureSettingReq",
            "read",
            PressureSettingReq.getReadInstance()
        )
    }

    @BleIsConnected
    private fun writePressureSetting() {
        if (!ensurePressureSettingSupported()) {
            return
        }
        executeSettingAction(
            LOG_TAG,
            "Set Pressure Setting",
            "PressureSettingReq",
            "enable=true",
            PressureSettingReq.getWriteInstance(true)
        )
    }

    @BleIsConnected
    private fun syncPressureData() {
        if (!isPressureSupported()) {
            return
        }
        commandHandle.executeReqCmd(
            PressureReq(0),
            ICommandResponse<PressureRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    XLog.tag("Pressure").d("sync pressure ok")
                }
            }
        )
        getString(R.string.qc_text_0093).showToast()
    }

    @BleIsConnected
    private fun startPressureMeasure() {
        if (!isPressureSupported()) {
            return
        }
        BleOperateManager.getInstance().manualModePressure(
            ICommandResponse<StartHeartRateRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    val value = rsp.getValue()
                    getString(R.string.qc_text_0086, value.toString()).showToast()
                }
            },
            false
        )
    }

    @BleIsConnected
    private fun stopPressureMeasure() {
        if (!isPressureSupported()) {
            return
        }
        BleOperateManager.getInstance().manualModePressure(
            ICommandResponse<StartHeartRateRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    (getString(R.string.qc_text_0085) + getString(R.string.qc_text_0053)).showToast()
                } else {
                    (getString(R.string.qc_text_0085) + getString(R.string.qc_text_0054)).showToast()
                }
            },
            true
        )
    }

    companion object {
        private const val LOG_TAG = "PressureSetting"
    }
}
