package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.HRVReq
import com.oudmon.ble.base.communication.req.HrvSettingReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.HRVRsp
import com.oudmon.ble.base.communication.rsp.StartHeartRateRsp
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivityHrvBinding
import com.qcwireless.sdksample.ext.showToast

class HrvActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivityHrvBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHrvBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0060)
        binding.fivSettingRead.setOnClickListener { readHrvSetting() }
        binding.fivSettingWrite.setOnClickListener { writeHrvSetting() }
        binding.fivDataSync.setOnClickListener { syncHrvData() }
        bindHealthQueryActions<HRVRsp>(
            todayView = binding.fivTodayData,
            singleDayView = binding.fivSingleDayData,
            rangeView = binding.fivRangeData,
            title = getString(R.string.qc_text_0060),
            supportCheck = { isHrvSupported() },
            todayAction = { callback ->
                BleOperateManager.getInstance().getTodayHrv(callback)
            },
            singleDayAction = { dayIndex, callback ->
                BleOperateManager.getInstance().getHrv(dayIndex, callback)
            },
            rangeAction = { startDayIndex, count, callback ->
                BleOperateManager.getInstance().getHrvs(startDayIndex, count, callback)
            }
        )
        binding.fivStartMeasure.setOnClickListener { startHrvMeasure() }
        binding.fivStopMeasure.setOnClickListener { stopHrvMeasure() }
        refreshSupportCache()
    }

    private fun isHrvSupported(): Boolean {
        return ensureSupported(setTimeCache.supportHrv)
    }

    @BleIsConnected
    private fun readHrvSetting() {
        if (!ensureHrvSettingSupported()) {
            return
        }
        executeSettingAction(
            LOG_TAG,
            "Get HRV Setting",
            "HrvSettingReq",
            "read",
            HrvSettingReq.getReadInstance()
        )
    }

    @BleIsConnected
    private fun writeHrvSetting() {
        if (!ensureHrvSettingSupported()) {
            return
        }
        executeSettingAction(
            LOG_TAG,
            "Set HRV Setting",
            "HrvSettingReq",
            "enable=true",
            HrvSettingReq.getWriteInstance(true)
        )
    }

    @BleIsConnected
    private fun syncHrvData() {
        if (!isHrvSupported()) {
            return
        }
        commandHandle.executeReqCmd(
            HRVReq(0),
            ICommandResponse<HRVRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    XLog.tag("HRV").d("sync hrv ok")
                }
            }
        )
        getString(R.string.qc_text_0093).showToast()
    }

    @BleIsConnected
    private fun startHrvMeasure() {
        if (!isHrvSupported()) {
            return
        }
        BleOperateManager.getInstance().manualModeHrv(
            ICommandResponse<StartHeartRateRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    val value = if (rsp.getHrv() > 0) rsp.getHrv() else rsp.getValue()
                    getString(R.string.qc_text_0074, value.toString()).showToast()
                }
            },
            false
        )
    }

    @BleIsConnected
    private fun stopHrvMeasure() {
        if (!isHrvSupported()) {
            return
        }
        BleOperateManager.getInstance().manualModeHrv(
            ICommandResponse<StartHeartRateRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    (getString(R.string.qc_text_0073) + getString(R.string.qc_text_0053)).showToast()
                } else {
                    (getString(R.string.qc_text_0073) + getString(R.string.qc_text_0054)).showToast()
                }
            },
            true
        )
    }

    companion object {
        private const val LOG_TAG = "HrvSetting"
    }
}
