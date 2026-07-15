package com.qcwireless.sdksample.activity

import android.os.Bundle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.entity.StartEndTimeEntity
import com.oudmon.ble.base.communication.req.BindAncsReq
import com.oudmon.ble.base.communication.req.DndReq
import com.oudmon.ble.base.communication.req.MusicSwitchReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.databinding.ActivityNotificationSettingBinding

class NotificationSettingActivity : BaseFunctionActivity() {

    private lateinit var binding: ActivityNotificationSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.title_notification)
        bindSettingAction(
            binding.fivSetAncs,
            LOG_TAG,
            "Set ANCS",
            "SetANCSReq",
            "call=true, sms=true, qq=true, wechat=true",
            ::ensureNotificationSupported
        ) { createSetAncsReq() }
        bindSettingAction(
            binding.fivGetAncs,
            LOG_TAG,
            "Get ANCS",
            "SimpleKeyReq(Constants.CMD_GET_ANCS_ON_OFF)",
            "cmd=CMD_GET_ANCS_ON_OFF",
            ::ensureNotificationSupported
        ) { SimpleKeyReq(Constants.CMD_GET_ANCS_ON_OFF) }
        bindSettingAction(
            binding.fivBindAncs,
            LOG_TAG,
            "Bind ANCS",
            "BindAncsReq",
            "none",
            ::ensureNotificationSupported
        ) { BindAncsReq() }
        bindSettingAction(
            binding.fivSetDnd,
            LOG_TAG,
            "Set DND",
            "DndReq",
            "isEnable=true, start=22:00, end=07:00",
            ::ensureNotificationSupported
        ) { DndReq.getWriteInstance(true, StartEndTimeEntity(22, 0, 7, 0)) }
        bindSettingAction(
            binding.fivGetDnd,
            LOG_TAG,
            "Get DND",
            "DndReq",
            "read",
            ::ensureNotificationSupported
        ) { DndReq.getReadInstance() }
        bindSettingAction(
            binding.fivSetMusicSwitch,
            LOG_TAG,
            "Set Music Switch",
            "MusicSwitchReq",
            "enable=true",
            ::ensureMusicSwitchSupported
        ) { MusicSwitchReq.getWriteInstance(true) }
        bindSettingAction(
            binding.fivGetMusicSwitch,
            LOG_TAG,
            "Get Music Switch",
            "MusicSwitchReq",
            "read",
            ::ensureMusicSwitchSupported
        ) { MusicSwitchReq.getReadInstance() }
        refreshCachesIfConnected()
    }

    override fun onResume() {
        super.onResume()
        refreshCachesIfConnected()
    }

    companion object {
        private const val LOG_TAG = "NotificationSetting"
    }
}
