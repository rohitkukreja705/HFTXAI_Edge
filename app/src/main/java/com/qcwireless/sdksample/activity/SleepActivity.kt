package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bean.SleepDisplay
import com.oudmon.ble.base.communication.ILargeDataLaunchSleepResponse
import com.oudmon.ble.base.communication.ILargeDataSleepResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.rsp.SleepNewProtoResp
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivitySleepBinding
import com.qcwireless.sdksample.ext.showToast

class SleepActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivitySleepBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0059)
        binding.fivDataSync.setOnClickListener { syncSleepData() }
        bindHealthQueryActions<SleepDisplay>(
            todayView = binding.fivTodayData,
            singleDayView = binding.fivSingleDayData,
            rangeView = binding.fivRangeData,
            title = getString(R.string.qc_text_0059),
            supportCheck = { isSleepSupported() },
            todayAction = { callback ->
                BleOperateManager.getInstance().getTodaySleep(callback)
            },
            singleDayAction = { dayIndex, callback ->
                BleOperateManager.getInstance().getSleep(dayIndex, callback)
            },
            rangeAction = { startDayIndex, count, callback ->
                BleOperateManager.getInstance().getSleeps(startDayIndex, count, callback)
            }
        )
        refreshSupportCache()
    }

    private fun isSleepSupported(): Boolean {
        return ensureSupported(setTimeCache.newSleepProtocol)
    }

    @BleIsConnected
    private fun syncSleepData() {
        if (!isSleepSupported()) {
            return
        }
        LargeDataHandler.getInstance().syncSleepList(
            0,
            object : ILargeDataSleepResponse {
                override fun sleepData(p0: SleepDisplay?) {
                    XLog.tag("Sleep").d("syncSleepData")
                }
            },
            object : ILargeDataLaunchSleepResponse {
                override fun sleepData(p0: SleepNewProtoResp?) {
                    XLog.tag("Sleep").d("syncLaunchSleepData")
                }
            }
        )
        getString(R.string.qc_text_0093).showToast()
    }
}
