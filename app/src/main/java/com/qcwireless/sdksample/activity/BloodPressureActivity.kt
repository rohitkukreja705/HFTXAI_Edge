package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.entity.StartEndTimeEntity
import com.oudmon.ble.base.communication.req.BpSettingReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.StartHeartRateRsp
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.entity.BlePressure
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivityBloodPressureBinding
import com.qcwireless.sdksample.ext.showToast

class BloodPressureActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivityBloodPressureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBloodPressureBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0061)
        binding.fivSettingRead.setOnClickListener { readBloodPressureSetting() }
        binding.fivSettingWrite.setOnClickListener { writeBloodPressureSetting() }
        binding.fivDataSync.setOnClickListener { syncBloodPressureData() }
        bindHealthQueryActions<List<BlePressure>>(
            todayView = binding.fivTodayData,
            singleDayView = binding.fivSingleDayData,
            rangeView = binding.fivRangeData,
            title = getString(R.string.qc_text_0061),
            supportCheck = { isBloodPressureSupported() },
            todayAction = { callback ->
                BleOperateManager.getInstance().getTodayBloodPressure(callback)
            },
            singleDayAction = { dayIndex, callback ->
                BleOperateManager.getInstance().getBloodPressure(dayIndex, callback)
            },
            rangeAction = { startDayIndex, count, callback ->
                BleOperateManager.getInstance().getBloodPressures(startDayIndex, count, callback)
            }
        )
        binding.fivStartMeasure.setOnClickListener { startBloodPressureMeasure() }
        binding.fivStopMeasure.setOnClickListener { stopBloodPressureMeasure() }
        refreshSupportCache()
    }

    private fun isBloodPressureSupported(): Boolean {
        return ensureSupported(setTimeCache.supportBloodPressure)
    }

    @BleIsConnected
    private fun readBloodPressureSetting() {
        if (!ensureBloodPressureSettingSupported()) {
            return
        }
        executeSettingAction(
            LOG_TAG,
            "Get BP Setting",
            "BpSettingReq",
            "read",
            BpSettingReq.getReadInstance()
        )
    }

    @BleIsConnected
    private fun writeBloodPressureSetting() {
        if (!ensureBloodPressureSettingSupported()) {
            return
        }
        executeSettingAction(
            LOG_TAG,
            "Set BP Setting",
            "BpSettingReq",
            "enable=true, start=09:00, end=18:00, multiple=60",
            BpSettingReq.getWriteInstance(true, StartEndTimeEntity(9, 0, 18, 0), 60)
        )
    }

    @BleIsConnected
    private fun syncBloodPressureData() {
        if (!isBloodPressureSupported()) {
            return
        }
        commandHandle.executeReqCmd(SimpleKeyReq(Constants.CMD_BP_TIMING_MONITOR_DATA)) { rsp ->
            if (rsp.status == BaseRspCmd.RESULT_OK) {
                XLog.tag("BP").d("sync blood pressure ok")
            }
        }
        getString(R.string.qc_text_0093).showToast()
    }

    @BleIsConnected
    private fun startBloodPressureMeasure() {
        if (!isBloodPressureSupported()) {
            return
        }
        BleOperateManager.getInstance().manualModeBP(
            ICommandResponse<StartHeartRateRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    val sbp = rsp.getSbp()
                    val dbp = rsp.getDbp()
                    getString(R.string.qc_text_0078, sbp.toString(), dbp.toString()).showToast()
                }
            },
            false
        )
    }

    @BleIsConnected
    private fun stopBloodPressureMeasure() {
        if (!isBloodPressureSupported()) {
            return
        }
        BleOperateManager.getInstance().manualModeBP(
            ICommandResponse<StartHeartRateRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    (getString(R.string.qc_text_0077) + getString(R.string.qc_text_0053)).showToast()
                } else {
                    (getString(R.string.qc_text_0077) + getString(R.string.qc_text_0054)).showToast()
                }
            },
            true
        )
    }

    companion object {
        private const val LOG_TAG = "BloodPressureSetting"
    }
}
