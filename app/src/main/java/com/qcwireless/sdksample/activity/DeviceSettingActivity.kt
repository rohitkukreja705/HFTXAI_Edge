package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.elvishew.xlog.XLog
import com.google.gson.Gson
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.entity.StartEndTimeEntity
import com.oudmon.ble.base.communication.req.AgpsReq
import com.oudmon.ble.base.communication.req.BaseReqCmd
import com.oudmon.ble.base.communication.req.BatterySavingReq
import com.oudmon.ble.base.communication.req.BindAncsReq
import com.oudmon.ble.base.communication.req.BloodOxygenSettingReq
import com.oudmon.ble.base.communication.req.BpSettingReq
import com.oudmon.ble.base.communication.req.BrightnessSettingsReq
import com.oudmon.ble.base.communication.req.DegreeSwitchReq
import com.oudmon.ble.base.communication.req.DeviceSupportReq
import com.oudmon.ble.base.communication.req.DialIndexReq
import com.oudmon.ble.base.communication.req.DisplayClockReq
import com.oudmon.ble.base.communication.req.DisplayOrientationReq
import com.oudmon.ble.base.communication.req.DisplayStyleReq
import com.oudmon.ble.base.communication.req.DisplayTimeReq
import com.oudmon.ble.base.communication.req.DndReq
import com.oudmon.ble.base.communication.req.HeartRateSettingReq
import com.oudmon.ble.base.communication.req.HrvSettingReq
import com.oudmon.ble.base.communication.req.IntellReq
import com.oudmon.ble.base.communication.req.MusicSwitchReq
import com.oudmon.ble.base.communication.req.MuslimReq
import com.oudmon.ble.base.communication.req.MuslimTargetReq
import com.oudmon.ble.base.communication.req.PalmScreenReq
import com.oudmon.ble.base.communication.req.PhoneIdReq
import com.oudmon.ble.base.communication.req.PressureSettingReq
import com.oudmon.ble.base.communication.req.ReadPersonalizationSettingReq
import com.oudmon.ble.base.communication.req.SetANCSReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.req.SugarLipidsSettingReq
import com.oudmon.ble.base.communication.req.TargetSettingReq
import com.oudmon.ble.base.communication.req.TemperatureSettingReq
import com.oudmon.ble.base.communication.req.TimeFormatReq
import com.oudmon.ble.base.communication.req.TouchControlReq
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivityDeviceSettingBinding
import com.qcwireless.sdksample.ext.showToast
import com.qcwireless.sdksample.view.FunctionItemView

class DeviceSettingActivity : BaseFunctionActivity() {

    private lateinit var binding: ActivityDeviceSettingBinding
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.qc_text_0015)
        bindNotificationActions()
        bindTargetAndProfileActions()
        bindHealthSettingActions()
        bindDisplayAndDeviceActions()
        bindAdvancedActions()
        refreshCachesIfConnected()
    }

    override fun onResume() {
        super.onResume()
        refreshCachesIfConnected()
    }

    override fun refreshCachesIfConnected() {
        if (BleOperateManager.getInstance().isConnected) {
            refreshSupportCache()
        }
    }

    private fun bindNotificationActions() {
        bindAction(
            binding.fivSetAncs,
            "Set ANCS",
            "SetANCSReq",
            "call=true, sms=true, qq=true, wechat=true",
            ::isNotificationSupported
        ) { createSetAncsReq() }

        bindAction(
            binding.fivGetAncs,
            "Get ANCS",
            "SimpleKeyReq(Constants.CMD_GET_ANCS_ON_OFF)",
            "cmd=CMD_GET_ANCS_ON_OFF",
            ::isNotificationSupported
        ) { SimpleKeyReq(Constants.CMD_GET_ANCS_ON_OFF) }

        bindAction(
            binding.fivBindAncs,
            "Bind ANCS",
            "BindAncsReq",
            "none",
            ::isNotificationSupported
        ) { BindAncsReq() }

        bindAction(
            binding.fivSetDnd,
            "Set DND",
            "DndReq",
            "isEnable=true, start=22:00, end=07:00",
            ::isNotificationSupported
        ) { DndReq.getWriteInstance(true, StartEndTimeEntity(22, 0, 7, 0)) }

        bindAction(
            binding.fivGetDnd,
            "Get DND",
            "DndReq",
            "read",
            ::isNotificationSupported
        ) { DndReq.getReadInstance() }

        bindAction(
            binding.fivSetMusicSwitch,
            "Set Music Switch",
            "MusicSwitchReq",
            "enable=true",
            ::isMusicSwitchSupported
        ) { MusicSwitchReq.getWriteInstance(true) }

        bindAction(
            binding.fivGetMusicSwitch,
            "Get Music Switch",
            "MusicSwitchReq",
            "read",
            ::isMusicSwitchSupported
        ) { MusicSwitchReq.getReadInstance() }
    }

    private fun bindTargetAndProfileActions() {
        bindAction(
            binding.fivSetTarget,
            "Set Target",
            "TargetSettingReq",
            "step=8000, calorie=500, distance=3000, sportMinute=60, sleepMinute=600"
        ) { TargetSettingReq.getWriteInstance(8000, 500, 3000, 60, 600) }

        bindAction(
            binding.fivGetTarget,
            "Get Target",
            "TargetSettingReq",
            "read"
        ) { TargetSettingReq.getReadInstance() }

        bindAction(
            binding.fivSetTimeFormat,
            "Set Time Format",
            "TimeFormatReq",
            "is24=true, metric=0"
        ) { TimeFormatReq.getWriteInstance(true, 0.toByte()) }

        bindAction(
            binding.fivGetTimeFormat,
            "Get Time Format",
            "TimeFormatReq",
            "read"
        ) { TimeFormatReq.getReadInstance() }

        bindAction(
            binding.fivSetUserProfile,
            "Set User Profile",
            "TimeFormatReq",
            "is24=true, metric=0, sex=0, age=30, height=170, weight=65, sbp=115, dbp=75, rateWarn=160"
        ) { TimeFormatReq.getWriteInstance(true, 0, 0, 30, 170, 65, 115, 75, 160) }

        bindAction(
            binding.fivGetUserProfile,
            "Get User Profile",
            "TimeFormatReq",
            "read"
        ) { TimeFormatReq.getReadInstance() }

        bindAction(
            binding.fivSetPhoneId,
            "Set Phone ID",
            "PhoneIdReq",
            "userId=123456789123"
        ) { PhoneIdReq.getWriteInstance("123456789123") }

        bindAction(
            binding.fivGetPhoneId,
            "Get Phone ID",
            "PhoneIdReq",
            "read"
        ) { PhoneIdReq.getReadInstance() }
    }

    private fun bindHealthSettingActions() {
        bindAction(
            binding.fivSetHeartRateSetting,
            "Set Heart Rate Setting",
            "HeartRateSettingReq",
            "enable=true, interval=10, tooLow=40, tooHigh=110",
            ::isHeartRateSettingSupported
        ) { HeartRateSettingReq.getWriteInstance(true, 10, 10, 40, 110) }

        bindAction(
            binding.fivGetHeartRateSetting,
            "Get Heart Rate Setting",
            "HeartRateSettingReq",
            "read",
            ::isHeartRateSettingSupported
        ) { HeartRateSettingReq.getReadInstance() }

        bindAction(
            binding.fivSetBloodOxygenSetting,
            "Set Blood Oxygen Setting",
            "BloodOxygenSettingReq",
            "enable=true, interval=2",
            ::isBloodOxygenSettingSupported
        ) { BloodOxygenSettingReq.getWriteInstance(true, 2.toByte()) }

        bindAction(
            binding.fivGetBloodOxygenSetting,
            "Get Blood Oxygen Setting",
            "BloodOxygenSettingReq",
            "read",
            ::isBloodOxygenSettingSupported
        ) { BloodOxygenSettingReq.getReadInstance() }

        bindAction(
            binding.fivSetBpSetting,
            "Set BP Setting",
            "BpSettingReq",
            "enable=true, start=09:00, end=18:00, multiple=60",
            ::isBloodPressureSettingSupported
        ) { BpSettingReq.getWriteInstance(true, StartEndTimeEntity(9, 0, 18, 0), 60) }

        bindAction(
            binding.fivGetBpSetting,
            "Get BP Setting",
            "BpSettingReq",
            "read",
            ::isBloodPressureSettingSupported
        ) { BpSettingReq.getReadInstance() }

        bindAction(
            binding.fivSetHrvSetting,
            "Set HRV Setting",
            "HrvSettingReq",
            "enable=true",
            ::isHrvSettingSupported
        ) { HrvSettingReq.getWriteInstance(true) }

        bindAction(
            binding.fivGetHrvSetting,
            "Get HRV Setting",
            "HrvSettingReq",
            "read",
            ::isHrvSettingSupported
        ) { HrvSettingReq.getReadInstance() }

        bindAction(
            binding.fivSetPressureSetting,
            "Set Pressure Setting",
            "PressureSettingReq",
            "enable=true",
            ::isPressureSettingSupported
        ) { PressureSettingReq.getWriteInstance(true) }

        bindAction(
            binding.fivGetPressureSetting,
            "Get Pressure Setting",
            "PressureSettingReq",
            "read",
            ::isPressureSettingSupported
        ) { PressureSettingReq.getReadInstance() }

        bindAction(
            binding.fivSetTemperatureSetting,
            "Set Temperature Setting",
            "TemperatureSettingReq",
            "enable=true, interval=2",
            ::isTemperatureSettingSupported
        ) { TemperatureSettingReq.getWriteInstance(true, 2.toByte()) }

        bindAction(
            binding.fivGetTemperatureSetting,
            "Get Temperature Setting",
            "TemperatureSettingReq",
            "read",
            ::isTemperatureSettingSupported
        ) { TemperatureSettingReq.getReadInstance() }

        bindAction(
            binding.fivSetSugarLipidsSetting,
            "Set Sugar/Lipids Setting",
            "SugarLipidsSettingReq",
            "type=1, enable=true, interval=2"
        ) { SugarLipidsSettingReq.getWriteInstance(1.toByte(), true, 2) }

        bindAction(
            binding.fivGetSugarLipidsSetting,
            "Get Sugar/Lipids Setting",
            "SugarLipidsSettingReq",
            "type=1"
        ) { SugarLipidsSettingReq.getReadInstance(1.toByte()) }
    }

    private fun bindDisplayAndDeviceActions() {
        bindAction(
            binding.fivSetBrightness,
            "Set Brightness",
            "BrightnessSettingsReq",
            "level=3"
        ) { BrightnessSettingsReq.getWriteInstance(3) }

        bindAction(
            binding.fivGetBrightness,
            "Get Brightness",
            "BrightnessSettingsReq",
            "read"
        ) { BrightnessSettingsReq.getReadInstance() }

        bindAction(
            binding.fivSetDisplayClock,
            "Set Display Clock",
            "DisplayClockReq",
            "enable=true"
        ) { DisplayClockReq.getWriteInstance(true) }

        bindAction(
            binding.fivGetDisplayClock,
            "Get Display Clock",
            "DisplayClockReq",
            "read"
        ) { DisplayClockReq.getReadInstance() }

        bindAction(
            binding.fivSetDisplayOrientation,
            "Set Display Orientation",
            "DisplayOrientationReq",
            "isPortrait=true, isLeft=true"
        ) { DisplayOrientationReq.getWriteInstance(true, true) }

        bindAction(
            binding.fivGetDisplayOrientation,
            "Get Display Orientation",
            "DisplayOrientationReq",
            "read"
        ) { DisplayOrientationReq.getReadInstance() }

        bindAction(
            binding.fivSetDisplayStyle,
            "Set Display Style",
            "DisplayStyleReq",
            "style=0"
        ) { DisplayStyleReq.getWriteInstance(0) }

        bindAction(
            binding.fivGetDisplayStyle,
            "Get Display Style",
            "DisplayStyleReq",
            "read"
        ) { DisplayStyleReq.getReadInstance() }

        bindAction(
            binding.fivSetDisplayTime,
            "Set Display Time",
            "DisplayTimeReq",
            "displayTime=1, displayType=0, alpha=100, total=1, curr=0, open=true"
        ) { DisplayTimeReq.getWriteInstanceNew(1, 0, 100, 1, 0, true) }

        bindAction(
            binding.fivGetDisplayTime,
            "Get Display Time",
            "DisplayTimeReq",
            "read"
        ) { DisplayTimeReq.getReadInstance() }

        bindAction(
            binding.fivSetDialIndex,
            "Set Dial Index",
            "DialIndexReq",
            "index=0"
        ) { DialIndexReq.getWriteInstance(0) }

        bindAction(
            binding.fivGetDialIndex,
            "Get Dial Index",
            "DialIndexReq",
            "read"
        ) { DialIndexReq.getReadInstance() }

        bindAction(
            binding.fivSetPalmScreen,
            "Set Palm Screen",
            "PalmScreenReq",
            "isEnable=true, isLeft=true",
            ::isGestureSupported
        ) { PalmScreenReq.getWriteInstance(true, true) }

        bindAction(
            binding.fivGetPalmScreen,
            "Get Palm Screen",
            "PalmScreenReq",
            "ringRead=true",
            ::isGestureSupported
        ) { PalmScreenReq.getRingReadInstance() }

        bindAction(
            binding.fivSetDegreeSwitch,
            "Set Degree Switch",
            "DegreeSwitchReq",
            "enable=true, isCelsius=true"
        ) { DegreeSwitchReq.getWriteInstance(true, true) }

        bindAction(
            binding.fivGetDegreeSwitch,
            "Get Degree Switch",
            "DegreeSwitchReq",
            "read"
        ) { DegreeSwitchReq.getReadInstance() }

        bindAction(
            binding.fivSetBatterySaving,
            "Set Battery Saving",
            "BatterySavingReq",
            "enable=true"
        ) { BatterySavingReq.getWriteInstance(true) }

        bindAction(
            binding.fivGetBatterySaving,
            "Get Battery Saving",
            "BatterySavingReq",
            "read"
        ) { BatterySavingReq.getReadInstance() }

        bindAction(
            binding.fivSetTouchControl,
            "Set Touch Control",
            "TouchControlReq",
            "type=9, touch=true, strength=5",
            ::isTouchSupported
        ) { TouchControlReq.getWriteInstance(9, true, 5) }

        bindAction(
            binding.fivGetTouchControl,
            "Get Touch Control",
            "TouchControlReq",
            "touch=true",
            ::isTouchSupported
        ) { TouchControlReq.getReadInstance(true) }
    }

    private fun bindAdvancedActions() {
        bindAction(
            binding.fivSetAgps,
            "Set AGPS",
            "AgpsReq",
            "enable=true"
        ) { AgpsReq.getWriteInstance(true) }

        bindAction(
            binding.fivGetAgps,
            "Get AGPS",
            "AgpsReq",
            "read"
        ) { AgpsReq.getReadInstance() }

        bindAction(
            binding.fivSetIntell,
            "Set Intell",
            "IntellReq",
            "isEnable=true, delaySecond=5"
        ) { IntellReq.getWriteInstance(true, 5.toByte()) }

        bindAction(
            binding.fivGetIntell,
            "Get Intell",
            "IntellReq",
            "read"
        ) { IntellReq.getReadInstance() }

        bindAction(
            binding.fivSetMuslim,
            "Set Muslim",
            "MuslimReq",
            "enable=true",
            ::isMuslimSupported
        ) { MuslimReq.getWriteInstance(true) }

        bindAction(
            binding.fivSetMuslimTarget,
            "Set Muslim Target",
            "MuslimTargetReq",
            "goal=33",
            ::isMuslimSupported
        ) { MuslimTargetReq(33) }

        bindAction(
            binding.fivGetDeviceSupport,
            "Get Device Support",
            "DeviceSupportReq",
            "read"
        ) { DeviceSupportReq.getReadInstance() }

        bindAction(
            binding.fivGetPersonalizationSetting,
            "Get Personalization Setting",
            "ReadPersonalizationSettingReq",
            "read"
        ) { ReadPersonalizationSettingReq.getReadInstance() }
    }

    private fun bindAction(
        view: FunctionItemView,
        actionName: String,
        requestName: String,
        defaultArgs: String,
        supportCheck: (() -> Boolean)? = null,
        requestProvider: () -> BaseReqCmd
    ) {
        view.setOnClickListener {
            if (supportCheck != null && !supportCheck()) {
                return@setOnClickListener
            }
            executeAction(actionName, requestName, defaultArgs, requestProvider())
        }
    }

    @BleIsConnected
    private fun executeAction(
        actionName: String,
        requestName: String,
        defaultArgs: String,
        request: BaseReqCmd
    ) {
        XLog.tag(LOG_TAG).d("action=$actionName request=$requestName defaultArgs=$defaultArgs")
        CommandHandle.getInstance().executeReqCmd(
            request,
            ICommandResponse<BaseRspCmd> { rsp ->
                val success = rsp.status == BaseRspCmd.RESULT_OK
                val resultText = gson.toJson(rsp)
                XLog.tag(LOG_TAG).d(
                    "action=$actionName request=$requestName defaultArgs=$defaultArgs success=$success status=${rsp.status} response=$resultText"
                )
                if (success) {
                    "$actionName success".showToast()
                } else {
                    "$actionName fail".showToast()
                }
            }
        )
    }

     override fun createSetAncsReq(): SetANCSReq {
        return SetANCSReq().apply {
            setCall(true)
            setSms(true)
            setQq(true)
            setWechat(true)
        }
    }

    private fun isNotificationSupported(): Boolean {
        return ensureSupported(deviceSupport.supportNotification || setTimeCache.supportWeChat)
    }

    private fun isMusicSwitchSupported(): Boolean {
        return ensureSupported(deviceSupport.supportRingMusic)
    }

    private fun isHeartRateSettingSupported(): Boolean {
        return ensureSupported(deviceSupport.supportHeartMeasure || setTimeCache.supportManualHeart)
    }

    private fun isBloodOxygenSettingSupported(): Boolean {
        return ensureSupported(setTimeCache.supportBloodOxygen || deviceSupport.supportIntervalBloodOxygen)
    }

    private fun isBloodPressureSettingSupported(): Boolean {
        return ensureSupported(setTimeCache.supportBloodPressure)
    }

    private fun isHrvSettingSupported(): Boolean {
        return ensureSupported(setTimeCache.supportHrv)
    }

    private fun isPressureSettingSupported(): Boolean {
        return ensureSupported(setTimeCache.supportPressure)
    }

    private fun isTemperatureSettingSupported(): Boolean {
        return ensureSupported(
            setTimeCache.supportTemperature ||
                    deviceSupport.supportSkinTemperature ||
                    deviceSupport.supportIntervalTemperature
        )
    }

    private fun isGestureSupported(): Boolean {
        return ensureSupported(deviceSupport.supportGesture)
    }

    private fun isTouchSupported(): Boolean {
        return ensureSupported(deviceSupport.supportTouch)
    }

    private fun isMuslimSupported(): Boolean {
        return ensureSupported(deviceSupport.supportMuslin)
    }

    companion object {
        private const val LOG_TAG = "DeviceSetting"
    }
}
