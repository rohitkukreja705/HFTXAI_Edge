package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.ReadDrinkAlarmReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivityDrinkReminderBinding
import com.qcwireless.sdksample.ext.showToast

class DrinkReminderActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivityDrinkReminderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrinkReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0066)
        binding.fivDataSync.setOnClickListener { syncDrinkReminder() }
        refreshSupportCache()
    }

    private fun isDrinkSupported(): Boolean {
        return ensureSupported(deviceSupport.supportDrink)
    }

    @BleIsConnected
    private fun syncDrinkReminder() {
        if (!isDrinkSupported()) {
            return
        }
        commandHandle.executeReqCmd(
            ReadDrinkAlarmReq(1),
            ICommandResponse<BaseRspCmd> { rsp ->
                if (rsp.status == BaseRspCmd.RESULT_OK) {
                    XLog.tag("Drink").d("sync drink reminder ok")
                }
            }
        )
        getString(R.string.qc_text_0093).showToast()
    }
}
