package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.ReadDetailSportDataReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.ReadDetailSportDataRsp
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.entity.BleStepDetails
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivityStepsBinding
import com.qcwireless.sdksample.ext.showToast

class StepsActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivityStepsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0058)
        binding.fivSyncTodaySteps.setOnClickListener { syncTodaySteps() }
        binding.fivSyncStepDetails.setOnClickListener { syncStepDetails() }
        bindHealthQueryActions<List<BleStepDetails>>(
            todayView = binding.fivTodayData,
            singleDayView = binding.fivSingleDayData,
            rangeView = binding.fivRangeData,
            title = getString(R.string.qc_text_0058),
            supportCheck = { isStepsSupported() },
            todayAction = { callback ->
                BleOperateManager.getInstance().getTodayStepDetail(callback)
            },
            singleDayAction = { dayIndex, callback ->
                BleOperateManager.getInstance().getStepDetail(dayIndex, callback)
            },
            rangeAction = { startDayIndex, count, callback ->
                BleOperateManager.getInstance().getStepDetails(startDayIndex, count, callback)
            }
        )
        refreshSupportCache()
    }

    private fun isStepsSupported(): Boolean {
        return ensureSupported(true)
    }

    @BleIsConnected
    private fun syncTodaySteps() {
        if (!isStepsSupported()) {
            return
        }
        commandHandle.executeReqCmd(SimpleKeyReq(Constants.CMD_GET_STEP_TODAY)) { rsp ->
            if (rsp.status == BaseRspCmd.RESULT_OK) {
                XLog.tag("Steps").d("sync today steps success")
            }
        }
        getString(R.string.qc_text_0093).showToast()
    }

    @BleIsConnected
    private fun syncStepDetails() {
        if (!isStepsSupported()) {
            return
        }
        commandHandle.executeReqCmd(
            ReadDetailSportDataReq(0, 0, 95),
            ICommandResponse<ReadDetailSportDataRsp> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    val size = rsp.getBleStepDetailses()?.size ?: 0
                    XLog.tag("Steps").d("sync step details size=$size")
                }
            }
        )
        getString(R.string.qc_text_0093).showToast()
    }
}
