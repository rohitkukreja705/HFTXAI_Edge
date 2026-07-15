package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.oudmon.ble.base.communication.req.BatterySavingReq
import com.oudmon.ble.base.communication.req.BrightnessSettingsReq
import com.oudmon.ble.base.communication.req.DegreeSwitchReq
import com.oudmon.ble.base.communication.req.DialIndexReq
import com.oudmon.ble.base.communication.req.DisplayClockReq
import com.oudmon.ble.base.communication.req.DisplayOrientationReq
import com.oudmon.ble.base.communication.req.DisplayStyleReq
import com.oudmon.ble.base.communication.req.DisplayTimeReq
import com.oudmon.ble.base.communication.req.PalmScreenReq
import com.oudmon.ble.base.communication.req.TouchControlReq
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.databinding.ActivityDisplayAndDeviceBinding
import com.qcwireless.sdksample.ext.startKtxActivity

class DisplayAndDeviceActivity : BaseFunctionActivity() {

    private lateinit var binding: ActivityDisplayAndDeviceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayAndDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.title_display_and_device)
        bindSettingAction(
            binding.fivSetBrightness,
            LOG_TAG,
            "Set Brightness",
            "BrightnessSettingsReq",
            "level=3"
        ) { BrightnessSettingsReq.getWriteInstance(3) }
        bindSettingAction(
            binding.fivGetBrightness,
            LOG_TAG,
            "Get Brightness",
            "BrightnessSettingsReq",
            "read"
        ) { BrightnessSettingsReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetDisplayClock,
            LOG_TAG,
            "Set Display Clock",
            "DisplayClockReq",
            "enable=true"
        ) { DisplayClockReq.getWriteInstance(true) }
        bindSettingAction(
            binding.fivGetDisplayClock,
            LOG_TAG,
            "Get Display Clock",
            "DisplayClockReq",
            "read"
        ) { DisplayClockReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetDisplayOrientation,
            LOG_TAG,
            "Set Display Orientation",
            "DisplayOrientationReq",
            "isPortrait=true, isLeft=true"
        ) { DisplayOrientationReq.getWriteInstance(true, true) }
        bindSettingAction(
            binding.fivGetDisplayOrientation,
            LOG_TAG,
            "Get Display Orientation",
            "DisplayOrientationReq",
            "read"
        ) { DisplayOrientationReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetDisplayStyle,
            LOG_TAG,
            "Set Display Style",
            "DisplayStyleReq",
            "style=0"
        ) { DisplayStyleReq.getWriteInstance(0) }
        bindSettingAction(
            binding.fivGetDisplayStyle,
            LOG_TAG,
            "Get Display Style",
            "DisplayStyleReq",
            "read"
        ) { DisplayStyleReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetDisplayTime,
            LOG_TAG,
            "Set Display Time",
            "DisplayTimeReq",
            "displayTime=1, displayType=0, alpha=100, total=1, curr=0, open=true"
        ) { DisplayTimeReq.getWriteInstanceNew(1, 0, 100, 1, 0, true) }
        bindSettingAction(
            binding.fivGetDisplayTime,
            LOG_TAG,
            "Get Display Time",
            "DisplayTimeReq",
            "read"
        ) { DisplayTimeReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetDialIndex,
            LOG_TAG,
            "Set Dial Index",
            "DialIndexReq",
            "index=0"
        ) { DialIndexReq.getWriteInstance(0) }
        bindSettingAction(
            binding.fivGetDialIndex,
            LOG_TAG,
            "Get Dial Index",
            "DialIndexReq",
            "read"
        ) { DialIndexReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetPalmScreen,
            LOG_TAG,
            "Set Palm Screen",
            "PalmScreenReq",
            "isEnable=true, isLeft=true",
            ::ensureGestureSupported
        ) { PalmScreenReq.getWriteInstance(true, true) }
        bindSettingAction(
            binding.fivGetPalmScreen,
            LOG_TAG,
            "Get Palm Screen",
            "PalmScreenReq",
            "ringRead=true",
            ::ensureGestureSupported
        ) { PalmScreenReq.getRingReadInstance() }
        bindSettingAction(
            binding.fivSetDegreeSwitch,
            LOG_TAG,
            "Set Degree Switch",
            "DegreeSwitchReq",
            "enable=true, isCelsius=true"
        ) { DegreeSwitchReq.getWriteInstance(true, true) }
        bindSettingAction(
            binding.fivGetDegreeSwitch,
            LOG_TAG,
            "Get Degree Switch",
            "DegreeSwitchReq",
            "read"
        ) { DegreeSwitchReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetBatterySaving,
            LOG_TAG,
            "Set Battery Saving",
            "BatterySavingReq",
            "enable=true"
        ) { BatterySavingReq.getWriteInstance(true) }
        bindSettingAction(
            binding.fivGetBatterySaving,
            LOG_TAG,
            "Get Battery Saving",
            "BatterySavingReq",
            "read"
        ) { BatterySavingReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetTouchControl,
            LOG_TAG,
            "Set Touch Control",
            "TouchControlReq",
            "type=9, touch=true, strength=5",
            ::ensureTouchSupported
        ) { TouchControlReq.getWriteInstance(9, true, 5) }
        bindSettingAction(
            binding.fivGetTouchControl,
            LOG_TAG,
            "Get Touch Control",
            "TouchControlReq",
            "touch=true",
            ::ensureTouchSupported
        ) { TouchControlReq.getReadInstance(true) }
        binding.fivWearCalibration.setOnClickListener {
            startKtxActivity<RevisionActivity>()
        }
        refreshCachesIfConnected()
    }

    override fun onResume() {
        super.onResume()
        refreshCachesIfConnected()
    }

    companion object {
        private const val LOG_TAG = "DisplayAndDevice"
    }
}
