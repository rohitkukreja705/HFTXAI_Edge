package com.qcwireless.sdksample.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.AppRevisionResp
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.responseImpl.DeviceNotifyListener
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.BatteryRsp
import com.oudmon.ble.base.communication.rsp.DeviceNotifyRsp
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils
import com.oudmon.ble.base.communication.utils.ByteUtil
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.databinding.ActivityRevisionBinding
import com.qcwireless.sdksample.event.BluetoothEvent
import com.qcwireless.sdksample.event.MessageEvent
import com.qcwireless.sdksample.ext.showToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RevisionActivity : BaseActivity() {
    private lateinit var binding: ActivityRevisionBinding
    private var listener = MyDeviceNotifyListener()

    private var btnClick = false
    private var isSuccess = false
    private var startToTest=false
    private val runnable = TimeoutRunnable()
    private lateinit var deviceNotifyListener: MyChangingDeviceNotifyListener

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRevisionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.revision_title)
//        Glide.with(this).asGif()
//            .load(R.mipmap.ring_1).into(binding.image2)
//        Glide.with(this).asGif()
//            .load(R.mipmap.ring_2).into(binding.image1)

        binding.btnConfirm.setOnClickListener {
            CommandHandle.getInstance()
                .executeReqCmd(
                    SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
                    ICommandResponse<BatteryRsp> {
                        val charging = it.isCharging
                        if (!charging) {
                            handler.removeCallbacks(runnable)
                            if (btnClick) {
                                BleOperateManager.getInstance().ringCalibration(false, null)
                                binding.btnConfirm.text = getString(R.string.revision_button_recalibrate)
                                startToTest=false
                            } else {
                                isSuccess = false
                                startToTest=true
                                handler.postDelayed(runnable, 2 * 60 * 1000)
                                binding.btnConfirm.text = getString(R.string.revision_button_calibrating)
                                BleOperateManager.getInstance().ringCalibration(true, listener)
                            }
                            btnClick = !btnClick
                        }else{
                            runOnUiThread {
                                getString(R.string.revision_toast_charging).showToast()
                            }
                        }
                    })

        }

    }



    inner class TimeoutRunnable : Runnable {
        override fun run() {
            runOnUiThread {
                btnClick = false
                startToTest=false
                binding.btnConfirm.text = getString(R.string.revision_button_recalibrate)
                getString(R.string.revision_toast_failed).showToast()
                BleOperateManager.getInstance().ringCalibration(false, null)
            }
        }
    }


    inner class MyDeviceNotifyListener : ICommandResponse<AppRevisionResp> {
        override fun onDataResponse(resultEntity: AppRevisionResp) {
            XLog.i(  resultEntity)
            XLog.tag("RevisionTTest").d("resultEntity success:" + resultEntity.success)
            runOnUiThread {
                when (resultEntity.success) {
                    REVISION_SUCCESS -> {
                        handler.removeCallbacks(runnable)
                        btnClick=false
                        startToTest=false
                        if (!isSuccess) {
                            isSuccess = true
                            getString(R.string.revision_toast_success).showToast()
                            binding.btnConfirm.text = getString(R.string.revision_button_start)
                        }
                    }
                    REVISION_TIMEOUT -> {
                        handler.removeCallbacks(runnable)
                        btnClick = false
                        startToTest = false
                        binding.btnConfirm.text = getString(R.string.revision_button_recalibrate)
                        getString(R.string.revision_toast_failed).showToast()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!BleOperateManager.getInstance().isConnected) {
            getString(R.string.revision_toast_connect_device_first).showToast()
            finish()
            return
        }

        deviceNotifyListener = MyChangingDeviceNotifyListener()
        BleOperateManager.getInstance().addOutDeviceListener(0x0c, deviceNotifyListener)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        super.onMessageEvent(messageEvent)
        when (messageEvent) {

            is BluetoothEvent -> {
                if (!messageEvent.connect) {
                    getString(R.string.revision_toast_connect_device_first).showToast()
                    finish()
                }
            }
        }

    }

    inner class MyChangingDeviceNotifyListener : DeviceNotifyListener() {
        override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
            if (resultEntity!!.status == BaseRspCmd.RESULT_OK) {
                XLog.i(  "设备触发刷新，数据类型:" + resultEntity.dataType)
                BleOperateManager.getInstance().removeOthersListener()
                when (resultEntity.dataType) {
                    0x0c -> {
                        XLog.i(  ByteUtil.bytesToString(resultEntity.loadData))
                        val charging = BLEDataFormatUtils.bytes2Int(
                            byteArrayOf(
                                resultEntity.loadData[2]
                            )
                        )
                        if(startToTest){
                            if (charging > 0) {
                                runOnUiThread {
                                    getString(R.string.revision_toast_charging).showToast()
                                    btnClick = false
                                    binding.btnConfirm.text = getString(R.string.revision_button_start)
                                    BleOperateManager.getInstance().ringCalibration(false, null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
        BleOperateManager.getInstance().ringCalibration(false, null)
    }

    companion object {
        const val REVISION_SUCCESS = 1
        const val REVISION_TIMEOUT = 3
    }
}
