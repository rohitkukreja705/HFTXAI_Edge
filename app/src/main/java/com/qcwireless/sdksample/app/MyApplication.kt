package com.qcwireless.sdksample.app

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleAction
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.qcwireless.sdksample.BuildConfig
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.cache.MMKVManager
import com.qcwireless.sdksample.recevier.BluetoothReceiver
import com.qcwireless.sdksample.recevier.MyBluetoothReceiver
import java.io.File
import kotlin.properties.Delegates

/**
 * @Author: Hzy
 * @CreateDate: 2021/6/25 11:50
 *
 * "程序应该是写给其他人读的,
 * 让机器来运行它只是一个附带功能"
 */
class MyApplication : Application(){

    var hardwareVersion: String = ""
    var firmwareVersion:String =""

    override fun onCreate() {
        super.onCreate()
       application = this
        initXLog()
        // 初始化MMKV
        MMKVManager.initialize(this)

        val intentFilter = BleAction.getIntentFilter()
        val myBleReceiver = MyBluetoothReceiver()

        registerInAppReceiver(myBleReceiver, intentFilter)

        initBle()
    }
    fun  initBle(){
        BleOperateManager.getInstance(this)
        BleOperateManager.getInstance().init()
//        BleOperateManager.getInstance().initRTKSPP(this)

        val deviceFilter: IntentFilter = BleAction.getDeviceIntentFilter()
        val deviceReceiver = BluetoothReceiver()
        // 添加导出状态参数
        registerExportReceiver(deviceReceiver, deviceFilter)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            this.registerReceiver(deviceReceiver, deviceFilter, RECEIVER_EXPORTED)
//        } else {
//            this.registerReceiver(deviceReceiver, deviceFilter)
//        }

        CONTEXT = applicationContext
    }

    fun initXLog(){
        val config = LogConfiguration.Builder()
            .logLevel(
                if (BuildConfig.DEBUG) {
                    LogLevel.ALL
                }else{
                    LogLevel.NONE
                }
            )
            .tag(getString(R.string.app_name)) // Specify TAG, default: "X-LOG"
            .enableThreadInfo() // Enable thread info, disabled by default
            .enableStackTrace(1) // Enable stack trace info with depth 2, disabled by default
            .enableBorder() // Enable border, disabled by default
            .build()


        XLog.init(
            config
        )
    }

    fun getDeviceIntentFilter(): IntentFilter? {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        return intentFilter
    }

    fun getAppRootFile(context: Context): File {
        // /storage/emulated/0/Android/data/pack_name/files
        return if(context.getExternalFilesDir("")!=null){
            context.getExternalFilesDir("")!!
        }else{
            val externalSaveDir = context.externalCacheDir
            externalSaveDir ?: context.cacheDir
        }

    }


    companion object {
        private var application: Application? = null
        var CONTEXT: Context by Delegates.notNull()
        fun getApplication(): Application? {
            if (application == null) {
                throw RuntimeException("Not support calling this, before create app or after terminate app.")
            }
            return application
        }

        val getInstance: MyApplication by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MyApplication()
        }
    }

    private fun registerInAppReceiver(receiver: BroadcastReceiver?, intentFilter: IntentFilter) {
        ContextCompat.registerReceiver(
            applicationContext,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun registerExportReceiver(receiver: BroadcastReceiver?, intentFilter: IntentFilter) {
        ContextCompat.registerReceiver(
            applicationContext,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }
}