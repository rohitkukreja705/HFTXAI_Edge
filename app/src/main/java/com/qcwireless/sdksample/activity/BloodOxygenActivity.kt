package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.BloodOxygenEntity
import com.oudmon.ble.base.communication.bigData.IntervalBloodOxygenEntity
import com.oudmon.ble.base.communication.req.BloodOxygenSettingReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.StartHeartRateRsp
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivityBloodOxygenBinding
import com.qcwireless.sdksample.ext.showToast

class BloodOxygenActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivityBloodOxygenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBloodOxygenBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0062)
        binding.fivSettingRead.setOnClickListener { readBloodOxygenSetting() }
        binding.fivSettingWrite.setOnClickListener { writeBloodOxygenSetting() }
        binding.fivDataSync.setOnClickListener { syncBloodOxygenData() }
        bindHealthQueryActions<BloodOxygenEntity>(
            todayView = binding.fivTodayData,
            singleDayView = binding.fivSingleDayData,
            rangeView = binding.fivRangeData,
            title = getString(R.string.qc_text_0062),
            supportCheck = { isBloodOxygenSupported() },
            todayAction = { callback ->
                BleOperateManager.getInstance().getTodayBloodOxygen(callback)
            },
            singleDayAction = { dayIndex, callback ->
                BleOperateManager.getInstance().getBloodOxygen(dayIndex, callback)
            },
            rangeAction = { startDayIndex, count, callback ->
                BleOperateManager.getInstance().getBloodOxygens(startDayIndex, count, callback)
            }
        )
        binding.fivStartMeasure.setOnClickListener { startBloodOxygenMeasure() }
        binding.fivStopMeasure.setOnClickListener { stopBloodOxygenMeasure() }
        refreshSupportCache()
    }

    private fun isBloodOxygenSupported(): Boolean {
        return ensureSupported(setTimeCache.supportBloodOxygen || deviceSupport.supportIntervalBloodOxygen)
    }

    @BleIsConnected
    private fun readBloodOxygenSetting() {
        if (!ensureBloodOxygenSettingSupported()) {
            return
        }
        executeSettingAction(
            LOG_TAG,
            "Get Blood Oxygen Setting",
            "BloodOxygenSettingReq",
            "read",
            BloodOxygenSettingReq.getReadInstance()
        )
    }

    @BleIsConnected
    private fun writeBloodOxygenSetting() {
        if (!ensureBloodOxygenSettingSupported()) {
            return
        }
        executeSettingAction(
            LOG_TAG,
            "Set Blood Oxygen Setting",
            "BloodOxygenSettingReq",
            "enable=true, interval=2",
            BloodOxygenSettingReq.getWriteInstance(true, 2.toByte())
        )
    }

    @BleIsConnected
    private fun syncBloodOxygenData() {
        if (!isBloodOxygenSupported()) {
            return
        }
        LargeDataHandler.getInstance().syncIntervalBloodOxygenWithCallback(0) { data ->
            val size = data?.array?.size ?: 0
            XLog.tag("SpO2").d("syncIntervalBloodOxygen size=$size")
        }
        getString(R.string.qc_text_0093).showToast()
    }

    @BleIsConnected
    private fun startBloodOxygenMeasure() {
        if (!isBloodOxygenSupported()) {
            return
        }
        BleOperateManager.getInstance().manualModeSpO2(
            ICommandResponse<StartHeartRateRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    val value = if (rsp.getBloodOxygen() > 0) rsp.getBloodOxygen() else rsp.getValue()
                    getString(R.string.qc_text_0082, value.toString()).showToast()
                }
            },
            false
        )
    }

    @BleIsConnected
    private fun stopBloodOxygenMeasure() {
        if (!isBloodOxygenSupported()) {
            return
        }
        BleOperateManager.getInstance().manualModeSpO2(
            ICommandResponse<StartHeartRateRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    (getString(R.string.qc_text_0081) + getString(R.string.qc_text_0053)).showToast()
                } else {
                    (getString(R.string.qc_text_0081) + getString(R.string.qc_text_0054)).showToast()
                }
            },
            true
        )
    }

    companion object {
        private const val LOG_TAG = "BloodOxygenSetting"
    }
}
