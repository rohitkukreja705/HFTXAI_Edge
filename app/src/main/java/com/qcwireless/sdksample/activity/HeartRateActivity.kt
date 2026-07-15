package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.bean.IntervalHeartRateEntity
import com.oudmon.ble.base.communication.bigData.resp.IIntervalHeartRateCallback
import com.oudmon.ble.base.communication.req.HeartRateSettingReq
import com.oudmon.ble.base.communication.req.ReadHeartRateReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.HeartRateSettingRsp
import com.oudmon.ble.base.communication.rsp.ReadHeartRateRsp
import com.oudmon.ble.base.communication.rsp.StartHeartRateRsp
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivityHeartRateBinding
import com.qcwireless.sdksample.ext.showToast
import com.qcwireless.sdksample.log.BluetoothLogManager
import java.util.TimeZone

class HeartRateActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivityHeartRateBinding
    private var lastHeartRateSetting: HeartRateSettingRsp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeartRateBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0041)

        binding.fivHeartRateSettingRead.setOnClickListener {
            readHeartRateSetting()
        }

        binding.fivHeartRateSettingWrite.setOnClickListener {
            writeHeartRateSetting()
        }

        binding.fivDataSync.setOnClickListener {
            syncHeartRateData()
        }
        bindHealthQueryActions<ReadHeartRateRsp>(
            todayView = binding.fivTodayData,
            singleDayView = binding.fivSingleDayData,
            rangeView = binding.fivRangeData,
            title = getString(R.string.qc_text_0041),
            todayAction = { callback ->
                BleOperateManager.getInstance().getTodayHeartRate(callback)
            },
            singleDayAction = { dayIndex, callback ->
                BleOperateManager.getInstance().getHeartRate(dayIndex, callback)
            },
            rangeAction = { startDayIndex, count, callback ->
                BleOperateManager.getInstance().getHeartRates(startDayIndex, count, callback)
            }
        )

        binding.fivStartMeasureOnce.setOnClickListener {
            startManualHeartRate()
        }

        binding.fivStopMeasureOnce.setOnClickListener {
            stopManualHeartRate()
        }


        refreshSupportCache()
        readHeartRateSetting()
    }
    @BleIsConnected
    private fun readHeartRateSetting() {
        commandHandle.executeReqCmd(
            HeartRateSettingReq.getReadInstance(),
            ICommandResponse<HeartRateSettingRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    lastHeartRateSetting = rsp
                    val content = "enable=${rsp.isEnable()} interval=${rsp.getStartInterval()} low=${rsp.getTooLowReminder()} high=${rsp.getTooHighReminder()} main=${rsp.getMainSwitch()}"
                    XLog.tag("HeartRateSetting").d(  content)
                    getString(R.string.qc_text_0046).showToast()
                }
            }
        )
    }
    @BleIsConnected
    private fun writeHeartRateSetting() {
        val req = HeartRateSettingReq.getWriteInstance(true, 10, 10, 40, 110)
        commandHandle.executeReqCmd(
            req,
            ICommandResponse<HeartRateSettingRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    lastHeartRateSetting = rsp
                    getString(R.string.qc_text_0047).showToast()
                }
            }
        )
    }
    @BleIsConnected
    private fun syncHeartRateData() {
        if (deviceSupport.supportRealTimeHr) {
            LargeDataHandler.getInstance().syncIntervalHeartRateWithCallback(
                0,
                object : IIntervalHeartRateCallback {
                    override fun readIntervalHeartRate(data: IntervalHeartRateEntity?) {
                        logHeartRateSyncDetail("Interval Sync", data)
                    }
                }
            )
            getString(R.string.qc_text_0048).showToast()
        } else {
            val nowTime = getNowUtcSeconds()
            commandHandle.executeReqCmd(
                ReadHeartRateReq(nowTime),
                ICommandResponse<ReadHeartRateRsp> { rsp ->
                    if (rsp.status == BaseRspCmd.RESULT_OK) {
                        logHeartRateSyncDetail("History Sync", rsp)
                    }
                }
            )
            getString(R.string.qc_text_0049).showToast()
        }
    }
    @BleIsConnected
    private fun startManualHeartRate( ) {
        if (!isManualHeartSupported()) {
            getString(R.string.qc_text_0050).showToast()
            return
        }

        BleOperateManager.getInstance().manualModeHeart(
            ICommandResponse<StartHeartRateRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    val value = if (rsp.getHeartRate() > 0) {
                        rsp.getHeartRate()
                    } else {
                        rsp.getValue()
                    }
                    getString(R.string.qc_text_0051, value.toString()).showToast()
                }
            },
            false
        )
    }

    @BleIsConnected()
    private fun stopManualHeartRate() {
        if (!isManualHeartSupported()) {
            getString(R.string.qc_text_0050).showToast()
            return
        }

        BleOperateManager.getInstance().manualModeHeart(
            ICommandResponse<StartHeartRateRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    getString(R.string.qc_text_0052)+getString(R.string.qc_text_0053).showToast()
                }else{
                    getString(R.string.qc_text_0052)+getString(R.string.qc_text_0054).showToast()
                }
            },
            true
        )
    }

    private fun isManualHeartSupported(): Boolean {
        val supportSetTime = setTimeCache.supportManualHeart
        val supportSetting = lastHeartRateSetting?.isEnable == true || lastHeartRateSetting?.mainSwitch == 1
        XLog.tag("HeartRateSupport").d("manualHeart setTime=$supportSetTime setting=$supportSetting")
        return  supportSetTime && supportSetting
    }

    private fun getNowUtcSeconds(): Long {
        val offsetSeconds = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000L
        return System.currentTimeMillis() / 1000L + offsetSeconds
    }

    private fun logHeartRateSyncDetail(scope: String, data: Any?) {
        val detail = HealthDataQueryLogFormatter.formatSuccess(
            title = getString(R.string.qc_text_0041),
            scope = scope,
            data = data
        )
        XLog.tag(SYNC_LOG_TAG).d(detail)
        BluetoothLogManager.addExternalLog(detail)
    }

    companion object {
        private const val SYNC_LOG_TAG = "HeartRateSync"
    }
}
