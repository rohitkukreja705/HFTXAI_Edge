package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.oudmon.ble.base.communication.req.PhoneIdReq
import com.oudmon.ble.base.communication.req.TargetSettingReq
import com.oudmon.ble.base.communication.req.TimeFormatReq
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.databinding.ActivityTargetAndProfileBinding

class TargetAndProfileActivity : BaseFunctionActivity() {

    private lateinit var binding: ActivityTargetAndProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTargetAndProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.title_target_and_profile)
        bindSettingAction(
            binding.fivSetTarget,
            LOG_TAG,
            "Set Target",
            "TargetSettingReq",
            "step=8000, calorie=500, distance=3000, sportMinute=60, sleepMinute=600"
        ) { TargetSettingReq.getWriteInstance(8000, 500, 3000, 60, 600) }
        bindSettingAction(
            binding.fivGetTarget,
            LOG_TAG,
            "Get Target",
            "TargetSettingReq",
            "read"
        ) { TargetSettingReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetTimeFormat,
            LOG_TAG,
            "Set Time Format",
            "TimeFormatReq",
            "is24=true, metric=0"
        ) { TimeFormatReq.getWriteInstance(true, 0.toByte()) }
        bindSettingAction(
            binding.fivGetTimeFormat,
            LOG_TAG,
            "Get Time Format",
            "TimeFormatReq",
            "read"
        ) { TimeFormatReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetUserProfile,
            LOG_TAG,
            "Set User Profile",
            "TimeFormatReq",
            "is24=true, metric=0, sex=0, age=30, height=170, weight=65, sbp=115, dbp=75, rateWarn=160"
        ) { TimeFormatReq.getWriteInstance(true, 0, 0, 30, 170, 65, 115, 75, 160) }
        bindSettingAction(
            binding.fivGetUserProfile,
            LOG_TAG,
            "Get User Profile",
            "TimeFormatReq",
            "read"
        ) { TimeFormatReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetPhoneId,
            LOG_TAG,
            "Set Phone ID",
            "PhoneIdReq",
            "userId=123456789123"
        ) { PhoneIdReq.getWriteInstance("123456789123") }
        bindSettingAction(
            binding.fivGetPhoneId,
            LOG_TAG,
            "Get Phone ID",
            "PhoneIdReq",
            "read"
        ) { PhoneIdReq.getReadInstance() }
    }

    companion object {
        private const val LOG_TAG = "TargetAndProfile"
    }
}
