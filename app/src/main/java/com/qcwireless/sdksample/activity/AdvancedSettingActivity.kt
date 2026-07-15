package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.oudmon.ble.base.communication.req.AgpsReq
import com.oudmon.ble.base.communication.req.DeviceSupportReq
import com.oudmon.ble.base.communication.req.IntellReq
import com.oudmon.ble.base.communication.req.MuslimReq
import com.oudmon.ble.base.communication.req.MuslimTargetReq
import com.oudmon.ble.base.communication.req.ReadPersonalizationSettingReq
import com.oudmon.ble.base.communication.req.TouchControlReq
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.databinding.ActivityAdvancedSettingBinding

class AdvancedSettingActivity : BaseFunctionActivity() {

    private lateinit var binding: ActivityAdvancedSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvancedSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.title_advanced)
        bindSettingAction(
            binding.fivSetAgps,
            LOG_TAG,
            "Set AGPS",
            "AgpsReq",
            "enable=true"
        ) { AgpsReq.getWriteInstance(true) }
        bindSettingAction(
            binding.fivGetAgps,
            LOG_TAG,
            "Get AGPS",
            "AgpsReq",
            "read"
        ) { AgpsReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetIntell,
            LOG_TAG,
            "Set Intell",
            "IntellReq",
            "isEnable=true, delaySecond=5"
        ) { IntellReq.getWriteInstance(true, 5.toByte()) }
        bindSettingAction(
            binding.fivGetIntell,
            LOG_TAG,
            "Get Intell",
            "IntellReq",
            "read"
        ) { IntellReq.getReadInstance() }
        bindSettingAction(
            binding.fivOpenMuslim,
            LOG_TAG,
            "Open Muslim",
            "TouchControlReq",
            "control=3, enable=true, delaySecond=5",
            ::ensureMuslimSupported
        ) {
            //Note:listener praise count in DeviceNotifyListener 0x25
            if(deviceSupport.supportRt11){
                TouchControlReq.getWriteTpSleepInstance(3, 1)

            }else{
                TouchControlReq.getWriteInstance(3, true, 1)
            }
        }
        bindSettingAction(
            binding.fivCloseMuslim,
            LOG_TAG,
            "Close Muslim",
            "TouchControlReq",
            "control=0, enable=true, delaySecond=5",
            ::ensureMuslimSupported
        ){
            if(deviceSupport.supportRt11){
                TouchControlReq.getWriteTpSleepInstance(0, 1)
            }else{
                TouchControlReq.getWriteInstance(0, true, 1)
            }
        }
        bindSettingAction(
            binding.fivSetMuslim,
            LOG_TAG,
            "Set Muslim",
            "MuslimReq",
            "enable=true",
            ::ensureMuslimSupported
        ) { MuslimReq.getWriteInstance(true) }
        bindSettingAction(
            binding.fivSetMuslimTarget,
            LOG_TAG,
            "Set Muslim Target",
            "MuslimTargetReq",
            "goal=33",
            ::ensureMuslimSupported
        ) { MuslimTargetReq(33) }
        bindSettingAction(
            binding.fivGetDeviceSupport,
            LOG_TAG,
            "Get Device Support",
            "DeviceSupportReq",
            "read"
        ) { DeviceSupportReq.getReadInstance() }
        bindSettingAction(
            binding.fivGetPersonalizationSetting,
            LOG_TAG,
            "Get Personalization Setting",
            "ReadPersonalizationSettingReq",
            "read"
        ) { ReadPersonalizationSettingReq.getReadInstance() }
        refreshCachesIfConnected()
    }

    override fun onResume() {
        super.onResume()
        refreshCachesIfConnected()
    }

    companion object {
        private const val LOG_TAG = "AdvancedSetting"
    }
}
