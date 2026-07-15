package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.Constants
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivitySedentaryReminderBinding
import com.qcwireless.sdksample.ext.showToast

class SedentaryReminderActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivitySedentaryReminderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySedentaryReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0067)
        binding.fivDataSync.setOnClickListener { syncSedentaryReminder() }
        refreshSupportCache()
    }

    private fun isSedentarySupported(): Boolean {
        return ensureSupported(deviceSupport.supportLongSit)
    }

    @BleIsConnected
    private fun syncSedentaryReminder() {
        if (!isSedentarySupported()) {
            return
        }
        commandHandle.executeReqCmd(SimpleKeyReq(Constants.CMD_GET_SIT_LONG)) { rsp ->
            if (rsp.status == BaseRspCmd.RESULT_OK) {
                XLog.tag("Sedentary").d("sync sedentary reminder ok")
            }
        }
        getString(R.string.qc_text_0093).showToast()
    }
}
