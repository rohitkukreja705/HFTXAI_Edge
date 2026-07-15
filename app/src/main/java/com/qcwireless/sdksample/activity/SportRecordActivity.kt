package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.communication.sport.SportPlusHandle
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivitySportRecordBinding
import com.qcwireless.sdksample.ext.showToast

class SportRecordActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivitySportRecordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySportRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0064)
        binding.fivDataSync.setOnClickListener { syncSportRecord() }
        refreshSupportCache()
    }

    private fun isSportSupported(): Boolean {
        return ensureSupported(true)
    }

    @BleIsConnected
    private fun syncSportRecord() {
        if (!isSportSupported()) {
            return
        }
        val syncSport = SportPlusHandle()
        syncSport.timeFormat = "yyyy-MM-dd HH:mm"
        syncSport.syncSportPlus { errorCode, _ ->
            XLog.tag("Sport").d("syncSportPlus code=$errorCode")
        }
        syncSport.cmdSummary(0)
        getString(R.string.qc_text_0093).showToast()
    }
}
