package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.oudmon.ble.base.communication.req.SugarLipidsSettingReq
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.databinding.ActivitySugarLipidsBinding

class SugarLipidsActivity : BaseFunctionActivity() {

    private lateinit var binding: ActivitySugarLipidsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySugarLipidsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.title_sugarlipids)
        bindSettingAction(
            binding.fivGetSugarLipidsSetting,
            LOG_TAG,
            "Get SugarLipids Setting",
            "SugarLipidsSettingReq",
            "type=1"
        ) { SugarLipidsSettingReq.getReadInstance(1.toByte()) }
        bindSettingAction(
            binding.fivSetSugarLipidsSetting,
            LOG_TAG,
            "Set SugarLipids Setting",
            "SugarLipidsSettingReq",
            "type=1, enable=true, interval=2"
        ) { SugarLipidsSettingReq.getWriteInstance(1.toByte(), true, 2) }
    }

    companion object {
        private const val LOG_TAG = "SugarLipids"
    }
}
