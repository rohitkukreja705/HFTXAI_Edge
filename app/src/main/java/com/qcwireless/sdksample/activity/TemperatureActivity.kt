package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.bigData.bean.IntervalTemperatureEntity
import com.oudmon.ble.base.communication.req.TemperatureSettingReq
import com.oudmon.ble.base.communication.file.FileHandle
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivityTemperatureBinding
import com.qcwireless.sdksample.ext.showToast

class TemperatureActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivityTemperatureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemperatureBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0065)
        binding.fivSettingRead.setOnClickListener { readTemperatureSetting() }
        binding.fivSettingWrite.setOnClickListener { writeTemperatureSetting() }
        binding.fivSyncAutoTemperature.setOnClickListener { syncAutoTemperature() }
        bindHealthQueryActions<IntervalTemperatureEntity>(
            todayView = binding.fivTodayData,
            singleDayView = binding.fivSingleDayData,
            rangeView = binding.fivRangeData,
            title = getString(R.string.qc_text_0065),
            supportCheck = { isTemperatureSupported() },
            todayAction = { callback ->
                BleOperateManager.getInstance().getTodayTemperature(callback)
            },
            singleDayAction = { dayIndex, callback ->
                BleOperateManager.getInstance().getTemperature(dayIndex, callback)
            },
            rangeAction = { startDayIndex, count, callback ->
                BleOperateManager.getInstance().getTemperatures(startDayIndex, count, callback)
            }
        )
        binding.fivSyncManualTemperature.setOnClickListener { syncManualTemperature() }
        refreshSupportCache()
    }

    private fun isTemperatureSupported(): Boolean {
        return ensureTemperatureSettingSupported()
    }

    @BleIsConnected
    private fun readTemperatureSetting() {
        if (!ensureTemperatureSettingSupported()) {
            return
        }
        executeSettingAction(
            LOG_TAG,
            "Get Temperature Setting",
            "TemperatureSettingReq",
            "read",
            TemperatureSettingReq.getReadInstance()
        )
    }

    @BleIsConnected
    private fun writeTemperatureSetting() {
        if (!ensureTemperatureSettingSupported()) {
            return
        }
        executeSettingAction(
            LOG_TAG,
            "Set Temperature Setting",
            "TemperatureSettingReq",
            "enable=true, interval=2",
            TemperatureSettingReq.getWriteInstance(true, 2.toByte())
        )
    }

    @BleIsConnected
    private fun syncAutoTemperature() {
        if (!isTemperatureSupported()) {
            return
        }
        FileHandle.getInstance().startObtainTemperatureSeries(2)
        XLog.tag("Temp").d("sync auto temperature")
        getString(R.string.qc_text_0093).showToast()
    }

    @BleIsConnected
    private fun syncManualTemperature() {
        if (!isTemperatureSupported()) {
            return
        }
        if (deviceSupport.supportNoSingleTemperature) {
            getString(R.string.qc_text_0092).showToast()
            return
        }
        FileHandle.getInstance().startObtainTemperatureOnce(0)
        XLog.tag("Temp").d("sync manual temperature")
        getString(R.string.qc_text_0093).showToast()
    }

    companion object {
        private const val LOG_TAG = "TemperatureSetting"
    }
}
