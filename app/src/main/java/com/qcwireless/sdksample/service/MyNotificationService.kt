package com.qcwireless.sdksample.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.RemoteController
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.oudmon.ble.base.util.MessPushUtil


/**
 * @author hzy ,
 * @date 2021/1/25
 * <p>
 * "程序应该是写给其他人读的,
 * 让机器来运行它只是一个附带功能"
 **/
class MyNotificationService : NotificationListenerService(),
    RemoteController.OnClientUpdateListener {
    private var mRemoteController: RemoteController? = null


    override fun onCreate() {
        super.onCreate()
        try {
            registerRemoteController()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        handlerNotification(sbn)
    }

    override fun onClientChange(clearing: Boolean) {

    }

    override fun onClientPlaybackStateUpdate(state: Int) {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onClientPlaybackStateUpdate(
        state: Int,
        stateChangeTimeMs: Long,
        currentPosMs: Long,
        speed: Float,
    ) {

    }

    override fun onClientTransportControlUpdate(transportControlFlags: Int) {

    }

    override fun onClientMetadataUpdate(metadataEditor: RemoteController.MetadataEditor?) {

    }

    private fun registerRemoteController() {
        mRemoteController = RemoteController(this, this)
        val registered: Boolean = try {
            (getSystemService(AUDIO_SERVICE) as AudioManager).registerRemoteController(
                mRemoteController
            )
        } catch (e: NullPointerException) {
            false
        } catch (e: SecurityException) {
            false
        }
        if (registered) {
            try {
                mRemoteController!!.setArtworkConfiguration(100, 100)
                mRemoteController!!.setSynchronizationMode(RemoteController.POSITION_SYNCHRONIZATION_CHECK)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }


    private fun handlerNotification(sbn: StatusBarNotification) {
        try {
            val notification = sbn.notification
            val packageName = sbn.packageName
            val tickerText = notification.tickerText
            var type = -1
//            PushMsgUintReq parameter description
//            type:
//            0x00: Call reminder 0x01: SMS reminder 0x02: QQ reminder 0x03: WeChat reminder,
//            0x04: incoming call to answer or hang up 0x05: Facebook message reminder 0x06: WhatsApp message reminder
//            0x07: Twitter message reminder 0x08: Skype message reminder 0x09: Line message reminder 0x0a: Linkedln
//            0x0b: Instagram 0x0c: TIM message 0x0d: Snapchat
//            0x0e: others other types of notifications

            if (packageName.contains("com.tencent.mobileqq")) {
                type = 0x02
            } else if (packageName.contains("com.twitter")) {
                type = 0x07
            } else if (packageName.contains("com.facebook")) {
                type = 0x05
            } else if (packageName.contains("com.whatsapp")) {
                type = 0x06
            } else if (packageName.contains("com.skype")) {
                type = 0x08
            } else if (packageName.contains("jp.naver.line")) {
                type = 0x09
            } else if (packageName.contains("com.linkedin.android")) {
                type = 0x0a
            } else if (packageName.contains("com.instagram.android")) {
                type = 0x0b
            } else {
                // others
            }
            if (type >= 0) {
                MessPushUtil.pushMsg(type, "your message")
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}