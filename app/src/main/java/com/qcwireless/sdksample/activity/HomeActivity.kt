package com.qcwireless.sdksample.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.elvishew.xlog.XLog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.req.DeviceSupportReq
import com.oudmon.ble.base.communication.req.FindDeviceReq
import com.oudmon.ble.base.communication.req.SetTimeReq
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.communication.responseImpl.DeviceNotifyListener
import com.oudmon.ble.base.communication.rsp.BatteryRsp
import com.oudmon.ble.base.communication.rsp.BaseRspCmd
import com.oudmon.ble.base.communication.rsp.DeviceNotifyRsp
import com.oudmon.ble.base.communication.rsp.DeviceSupportFunctionRsp
import com.oudmon.ble.base.communication.rsp.SetTimeRsp
import com.oudmon.ble.base.communication.utils.BLEDataFormatUtils
import com.oudmon.ble.base.communication.utils.ByteUtil
import com.oudmon.ble.base.util.DateUtil
import com.qcwireless.sdksample.app.MyApplication
import com.qcwireless.sdksample.event.BluetoothEvent
import com.qcwireless.sdksample.event.FirmwareVersionEvent
import com.qcwireless.sdksample.utils.BluetoothUtils
import com.qcwireless.sdksample.BuildConfig
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.bean.HomeConnectionActionVisibility
import com.qcwireless.sdksample.bean.HomeFirmwareVersionVisibility
import com.qcwireless.sdksample.cache.DeviceFunctionSupport
import com.qcwireless.sdksample.cache.SetTime
import com.qcwireless.sdksample.databinding.ActivityHomeBinding
import com.qcwireless.sdksample.dialog.NotificationDialog
import com.qcwireless.sdksample.event.MessageEvent
import com.qcwireless.sdksample.ext.showToast
import com.qcwireless.sdksample.ext.startKtxActivity
import com.qcwireless.sdksample.utils.hasBluetooth
import com.qcwireless.sdksample.utils.requestBluetoothPermission
import com.qcwireless.sdksample.utils.requestLocationPermission
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 功能列表演示页面
 */
class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var myDeviceNotifyListener: MyDeviceNotifyListener


    companion object {
        //Your key，Only for register and unregister
        private const val MAIN_KEY = 11
    }

    override fun getStatusBarColorRes() = R.color.bg_primary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.run {
            tvVersion.text = getString(R.string.app_name)+":"+BuildConfig.VERSION_NAME
        }
        myDeviceNotifyListener = MyDeviceNotifyListener()
        BleOperateManager.getInstance()
            .addOutDeviceListener(MAIN_KEY, myDeviceNotifyListener)
        // 扫描设备
        binding.fivScan.setOnClickListener {
            requestLocationPermission(this@HomeActivity, PermissionCallback())
        }

        // 重连设备
        binding.fivReconnect.setOnClickListener {
            reconnectDevice()
        }

        // 断开连接
        binding.fivDisconnect.setOnClickListener {
            disconnectDevice()
        }

        // 蓝牙配对
        binding.fivBtPair.setOnClickListener {
            createBond()
        }

        binding.fivUnbindDevice.setOnClickListener {
            unbindDevice()
        }

        // 查找设备
        binding.fivFindDevice.setOnClickListener {
            findDevice()
        }

        binding.fivHealthSettings.setOnClickListener {
            startKtxActivity<HealthSettingsActivity>()
        }

        binding.fivTargetAndProfile.setOnClickListener {
            startKtxActivity<TargetAndProfileActivity>()
        }

        binding.fivDisplayAndDevice.setOnClickListener {
            startKtxActivity<DisplayAndDeviceActivity>()
        }

        binding.fivNotification.setOnClickListener {
            startKtxActivity<NotificationSettingActivity>()
        }

        binding.fivAdvanced.setOnClickListener {
            startKtxActivity<AdvancedSettingActivity>()
        }

        // 通过蓝牙升级
        binding.fivViaBleUpdate.setOnClickListener {
            startKtxActivity<OtaActivity>()
        }

        // ═══ NEW HFTX AI CARD NAVIGATION ═══
        binding.btnAiReport.setOnClickListener {
            startKtxActivity<AiReportActivity>()
        }
        binding.cardHeartRate.setOnClickListener {
            startKtxActivity<HeartRateActivity>()
        }
        binding.cardSpo2.setOnClickListener {
            startKtxActivity<BloodOxygenActivity>()
        }
        binding.cardSleep.setOnClickListener {
            startKtxActivity<SleepActivity>()
        }
        binding.cardHrv.setOnClickListener {
            startKtxActivity<HrvActivity>()
        }
        binding.cardSteps.setOnClickListener {
            startKtxActivity<StepsActivity>()
        }
        binding.cardStress.setOnClickListener {
            startKtxActivity<PressureActivity>()
        }
        binding.navHealth.setOnClickListener {
            startKtxActivity<HealthSettingsActivity>()
        }
        binding.navProfile.setOnClickListener {
            startKtxActivity<TargetAndProfileActivity>()
        }
        binding.navDisplay.setOnClickListener {
            startKtxActivity<DisplayAndDeviceActivity>()
        }
        binding.navNotify.setOnClickListener {
            startKtxActivity<NotificationSettingActivity>()
        }
        binding.navAdvanced.setOnClickListener {
            startKtxActivity<AdvancedSettingActivity>()
        }
        binding.navOta.setOnClickListener {
            startKtxActivity<OtaActivity>()
        }
        // Quick scan / reconnect on device card
        binding.btnScanNew.setOnClickListener {
            requestLocationPermission(this@HomeActivity, PermissionCallback())
        }
        binding.btnReconnectNew.setOnClickListener {
            reconnectDevice()
        }
        binding.btnDisconnectNew.setOnClickListener {
            disconnectDevice()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (!BluetoothUtils.isEnabledBluetooth(this)) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                }
                startActivityForResult(intent, 300)
            }
        } catch (e: Exception) {
        }
        if (!hasBluetooth(this)) {
            requestBluetoothPermission(this, BluetoothPermissionCallback())
        }

        val deviceName = DeviceManager.getInstance().deviceName.orEmpty()
        val deviceMac = DeviceManager.getInstance().deviceAddress.orEmpty()

        binding.tvDeviceName.text =
            getString(R.string.qc_text_0009) + deviceName
        binding.tvDeviceMac.text =
            getString(R.string.qc_text_0010) + deviceMac
        // Mirror to new UI cards
        try {
            binding.tvDeviceNameDisplay.text = deviceName.ifEmpty { "No device paired" }
            binding.tvDeviceMacDisplay.text = deviceMac.ifEmpty { "Tap scan to connect" }
        } catch (e: Exception) { /* non-fatal */ }
        if (BleOperateManager.getInstance().isConnected) {
            showConnectedState()
            requestBatteryStatus()
        } else {
            showDisconnectedState()
        }

     }

    inner class MyDeviceNotifyListener : DeviceNotifyListener() {
        override fun onDataResponse(resultEntity: DeviceNotifyRsp?) {
            if (resultEntity!!.status == BaseRspCmd.RESULT_OK) {
                //数据同步中的话，应该返回不处理回�?
                BleOperateManager.getInstance().removeOthersListener()
                when (resultEntity.dataType) {
                    1 -> {
//                        viewModel.syncTodayHeartSingleData()
                    }

                    2 -> {
//                        viewModel.syncBpSingle()
                    }

                    3 -> {
//                        viewModel.syncTodaySpo2Single(0x00)
                    }

                    4 -> {
//                        viewModel.syncTodayStepDetailSingle()
                    }

                    5 -> {
//                        viewModel.syncTodayTemperatureSingle()
                    }

                    7 -> {
//                        sportRunning = false
//                        sportType = 0
//                        sportStatus = 0
//                        viewModel.syncTodaySportPlusDetailSingle()
                    }

                    9, 0x0b -> {
//                        EventBus.getDefault().post(DeviceToAppSyncEvent(resultEntity.dataType))
                    }

                    0x0c -> {
                        try {
                            val battery = BLEDataFormatUtils.bytes2Int(
                                byteArrayOf(
                                    resultEntity.loadData[1]
                                )
                            )
                            val charging = BLEDataFormatUtils.bytes2Int(
                                byteArrayOf(
                                    resultEntity.loadData[2]
                                )
                            )
                            updateBatteryStatus(battery, charging == 1)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    0x0d -> {
//                        viewModel.syncTodayBloodSugar(0x00)
                    }

                    0x10 -> {
//                        CommandHandle.getInstance().executeReqCmd(
//                            TargetSettingReq.getReadInstance(), ICommandResponse<TargetSettingRsp> {
//                                XLog.i(it)
//                                if (it.action == TargetSettingRsp.ACTION_READ) {
//                                    ktxRunOnBgSingle {
//                                        if (it.step > 0) {
//                                            val target =
//                                                TargetEntity(
//                                                    UserConfig.getInstance().deviceAddressNoClear,
//                                                    it.step,
//                                                    it.calorie / 1000f,
//                                                    it.distance / 1000f,
//                                                    3f,
//                                                    8f, 1320, 540, 660
//                                                )
//                                            UserProfileRepository.getInstance.insertTarget(target)
//                                            EventBus.getDefault().post(DeviceNotifyTypeEvent(0x10))
//                                        }
//                                    }
//                                }
//                            }
//                        )
                    }

                    0x27 -> {
//                        viewModel.syncTodayTemperatureSingle()
                    }

                    0x2b -> {
//                        viewModel.syncTodayHrvSingle()
                    }

                    0x2c -> {
//                        viewModel.syncTodayPressureSingle()
                    }

                    0x29 -> {
//                        EventBus.getDefault().post(RingGameDataEvent())
                    }

                    0x25 -> {
                        //732500000013000700000000000000b2
                        val count = BLEDataFormatUtils.bytes2Int(
                            byteArrayOf(
                                resultEntity.loadData[1],
                                resultEntity.loadData[2],
                                resultEntity.loadData[3],
                                resultEntity.loadData[4],
                            )
                        )
                       getString(R.string.praise_count).format(count) .showToast()

//                        XLog.i(count)
//                        EventBus.getDefault().post(RingGameDataEvent())
//                        EventBus.getDefault().post(DataEvent(count, false))
//                        ktxRunOnBgSingle {
//                            Repository.getInstance.saveTotal(count)
//                            muslimTemp++
//                            ktxRunOnUi {
//                                try {
//                                    if (!muslimRefresh || muslimTemp > 10) {
//                                        muslimRefresh = true
//                                        muslimTemp = 0
//                                        viewModel.syncTodayOnly(object :
//                                            BaseDeviceResult<Rsp> {
//                                            override fun result(errorCode: Int, t: Rsp) {
//                                                ktxRunOnUi {
//                                                    healthyAdapter.updateCount(count, true)
//                                                }
//                                            }
//
//                                        })
//                                        if (count > 1) {
//                                            healthyModule()
//                                        }
//                                    }
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }
//                        ktxRunOnUi {
//                            try {
//                                if (count >= UserConfig.getInstance().muslimCustomTarget) {
//                                    if (UserConfig.getInstance().muslimNotification != DateUtil().y_M_D) {
//                                        UserConfig.getInstance().muslimNotification =
//                                            DateUtil().y_M_D
//                                        UserConfig.getInstance().save()
//                                        XLog.i("----notification")
//                                        NotificationUtils(context).initMuslinNotification()
//                                    }
//                                } else {
//                                    XLog.i("----notification11")
//                                }
//                                healthyAdapter.updateCount(count, true)
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
                    }

                    0x11 -> {
                        //亮屏和左右佩戴（屏幕方向切换�?
                        XLog.i(ByteUtil.bytesToString(resultEntity.loadData))
                        //屏幕 0 �?1开
                        val open = BLEDataFormatUtils.bytes2Int(
                            byteArrayOf(
                                resultEntity.loadData[2]
                            )
                        )
                        //1左手佩戴 2右手佩戴
                        val wear = BLEDataFormatUtils.bytes2Int(
                            byteArrayOf(
                                resultEntity.loadData[2]
                            )
                        )
                        if (open == 1) {
                            if (wear == 1) {
                                XLog.i("Screen----left")
                            } else {
                                XLog.i("Screen----right")
                            }
                        } else {
                            XLog.i("Screen----close")
                        }

                    }

                    0x12 -> {
                        //step calorie  distance update reminder
                        //7312 00005200025100003c0000000066
                        XLog.i(ByteUtil.bytesToString(resultEntity.loadData))

                        val step = BLEDataFormatUtils.bytes2Int(
                            byteArrayOf(
                                resultEntity.loadData[1],
                                resultEntity.loadData[2],
                                resultEntity.loadData[3]
                            )
                        )

                        val calorie = BLEDataFormatUtils.bytes2Int(
                            byteArrayOf(
                                resultEntity.loadData[4],
                                resultEntity.loadData[5],
                                resultEntity.loadData[6]
                            )
                        )
                        val distance = BLEDataFormatUtils.bytes2Int(
                            byteArrayOf(
                                resultEntity.loadData[7],
                                resultEntity.loadData[8],
                                resultEntity.loadData[9]
                            )
                        )

                        val content = getString(
                            R.string.qc_text_0031,
                            step.toString(),
                            calorie.toString(),
                            distance.toString()
                        )
                        XLog.d(content)
                        content.showToast()
                    }

                    0x31 -> {
                        // 运动心率预警提醒上报
                        val heartValue = ByteUtil.byteToInt(resultEntity.loadData[1]).toString()
                        val content = getString(
                            R.string.qc_text_0030,
                            heartValue
                        )
                        showCmdRemindDialog(content)
                    }

                    0x32 -> {
                        // 喝水提醒上报
                        showCmdRemindDialog(
                            getString(R.string.qc_text_0029)
                        )
                    }

                    0x33 -> {
                        //久坐提醒上报
                        showCmdRemindDialog(getString(R.string.qc_text_0028))
                    }

                    0x34 -> {
                        //闹钟提醒上报
                        val dateNow = DateUtil()
                        val nowMinute = dateNow.hour * 60 + dateNow.minute
                        //Your cache AlarmTime
                        val saveTime = dateNow.hour * 60 + dateNow.minute
                        if (saveTime == nowMinute) {
                            showCmdRemindDialog(getString(R.string.qc_text_0027))
                        }
                    }

//                    0x37 -> {
//                        //设备端心率测量结果上�?
//                        val heartValue = ByteUtil.byteToInt(resultEntity.loadData[1])
//                    }

//                    0x38 -> {
//                        val praiseCount = ByteUtil.bytesToInt(
//                            byteArrayOf(
//                                resultEntity.loadData[2],
//                                resultEntity.loadData[3]
//                            )
//                        )
//                        EventBus.getDefault().post(DeviceCustomerPraiseCountEvent(praiseCount))
//                    }

                    0x39 -> {
                        //生理周期提醒
                        //1 经期 2排卵�?
                        val type = ByteUtil.byteToInt(resultEntity.loadData[1])
                        //day later
                        val day = ByteUtil.byteToInt(resultEntity.loadData[2])
                        val content = if (type == 1) {
                            getString(R.string.qc_text_0025, day.toString())
                        } else {
                            getString(R.string.qc_text_0026, day.toString())
                        }
                        showCmdRemindDialog(content)
                    }

                    0x3A -> {
                        //设备端心率过高过低提醒上�?
                        val type = ByteUtil.byteToInt(resultEntity.loadData[1])
                        //心率�?如果设备支持
                        val value = ByteUtil.byteToInt(resultEntity.loadData[2])

                        if (type == 1) {
                            showCmdRemindDialog(getString(R.string.qc_text_0021) + ":$value", "")
                        } else if (type == 2) {
                            showCmdRemindDialog(getString(R.string.qc_text_0022) + ":$value", "")
                        }
                    }
                }
            }
        }
    }

    fun showCmdRemindDialog(content: String, title: String = getString(R.string.qc_text_0024)) {

        val dialog = NotificationDialog.Builder()
            .setConfirmMessage(getString(R.string.qc_text_0023))
            .setTitle(title)
            .setContent(content).build()

        if (!supportFragmentManager.isStateSaved) {
            dialog.show(supportFragmentManager, "showCmdRemindDialog")
        } else {
            window?.decorView?.post {
                if (!isFinishing && !supportFragmentManager.isStateSaved) {
                    dialog.show(supportFragmentManager, "showCmdRemindDialog")
                }
            }
        }
        dialog.setOnConfirmListener {}
    }

    inner class PermissionCallback : OnPermissionCallback {
        override fun onGranted(permissions: MutableList<String>, all: Boolean) {
            if (!all) {

            } else {
                startKtxActivity<DeviceBindActivity>()
            }
        }

        override fun onDenied(permissions: MutableList<String>, never: Boolean) {
            super.onDenied(permissions, never)
            if (never) {
                XXPermissions.startPermissionActivity(this@HomeActivity, permissions);
            }
        }

    }

    inner class BluetoothPermissionCallback : OnPermissionCallback {
        override fun onGranted(permissions: MutableList<String>, all: Boolean) {
            if (!all) {

            }
        }

        override fun onDenied(permissions: MutableList<String>, never: Boolean) {
            super.onDenied(permissions, never)
            if (never) {
                XXPermissions.startPermissionActivity(this@HomeActivity, permissions)
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        if(messageEvent is BluetoothEvent) {
            if (messageEvent.connect) {
                CommandHandle.getInstance()
                    .executeReqCmd(SetTimeReq(1), ICommandResponse<SetTimeRsp>() {
                        GlobalScope.launch {
                            SetTime.instance.updateFrom(it)
                        }
                    })
                CommandHandle.getInstance()
                    .executeReqCmd(
                        DeviceSupportReq.getReadInstance(),
                        ICommandResponse<DeviceSupportFunctionRsp> {
                            DeviceFunctionSupport.instance.updateFrom(it)
                        })
                showConnectedState()
                requestBatteryStatus()
            } else {
                showDisconnectedState()
            }
        }
    }

    private fun showConnectedState() {
        binding.tvBleStatus.text =
            getString(R.string.qc_text_0011) + getString(R.string.qc_text_0012)
        refreshConnectionActionState(isConnected = true)
        refreshFirmwareVersionState(isConnected = true)
        // Update new UI
        try {
            binding.tvBleStatusChip.text = "● Online"
            binding.tvBleStatusChip.setTextColor(
                androidx.core.content.ContextCompat.getColor(this, R.color.hftx_teal))
            binding.tvBleStatusChip.background =
                androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_status_connected)
            binding.btnDisconnectNew.visibility = android.view.View.VISIBLE
        } catch (e: Exception) { /* non-fatal UI update */ }
    }

    private fun showDisconnectedState() {
        binding.tvBleStatus.text =
            getString(R.string.qc_text_0011) + getString(R.string.qc_text_0013)
        binding.tvBatteryStatus.visibility = View.GONE
        refreshConnectionActionState(isConnected = false)
        refreshFirmwareVersionState(isConnected = false)
        // Update new UI
        try {
            binding.tvBleStatusChip.text = "● Offline"
            binding.tvBleStatusChip.setTextColor(
                androidx.core.content.ContextCompat.getColor(this, R.color.hftx_red))
            binding.tvBleStatusChip.background =
                androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_status_disconnected)
            binding.btnDisconnectNew.visibility = android.view.View.GONE
        } catch (e: Exception) { /* non-fatal UI update */ }
    }

    private fun refreshConnectionActionState(
        isConnected: Boolean = BleOperateManager.getInstance().isConnected,
        deviceMac: String = DeviceManager.getInstance().deviceAddress.orEmpty()
    ) {
        val uiState = HomeConnectionActionVisibility.resolve(
            isConnected = isConnected,
            deviceMac = deviceMac
        )
        binding.fivReconnect.visibility = if (uiState.reconnectVisible) View.VISIBLE else View.GONE
        binding.fivDisconnect.visibility = if (uiState.disconnectVisible) View.VISIBLE else View.GONE
        binding.fivBtPair.visibility = if (uiState.btPairVisible) View.VISIBLE else View.GONE
        binding.fivUnbindDevice.visibility = if (uiState.unbindVisible) View.VISIBLE else View.GONE
        binding.fivFindDevice.visibility = if (uiState.findDeviceVisible) View.VISIBLE else View.GONE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFirmwareVersionEvent(messageEvent: FirmwareVersionEvent) {
        refreshFirmwareVersionState(firmwareVersion = messageEvent.firmwareVersion)
    }

    private fun refreshFirmwareVersionState(
        isConnected: Boolean = BleOperateManager.getInstance().isConnected,
        firmwareVersion: String = MyApplication.getInstance.firmwareVersion
    ) {
        val uiState = HomeFirmwareVersionVisibility.resolve(
            isConnected = isConnected,
            firmwareVersion = firmwareVersion
        )
        binding.tvFirmwareVersion.visibility = if (uiState.visible) View.VISIBLE else View.GONE
        if (uiState.visible) {
            binding.tvFirmwareVersion.text =
                getString(R.string.qc_text_0104, uiState.firmwareVersion)
        }
    }

    private fun requestBatteryStatus() {
        XLog.tag("BatteryTTest").d("requestBatteryStatus: start")
        CommandHandle.getInstance()
            .executeReqCmd(
                SimpleKeyReq(Constants.CMD_GET_DEVICE_ELECTRICITY_VALUE),
                ICommandResponse<BatteryRsp> {
                    XLog.tag("BatteryTTest").d("requestBatteryStatus:end:status"+it.status+"battery:"+it.batteryValue)
                    if (it.status == BaseRspCmd.RESULT_OK) {
                        updateBatteryStatus(it.batteryValue, it.isCharging)
                    }
                })
    }

    private fun updateBatteryStatus(battery: Int, isCharging: Boolean) {
        binding.root.post {
            if (!BleOperateManager.getInstance().isConnected) {
                binding.tvBatteryStatus.visibility = View.GONE
                return@post
            }
            binding.tvBatteryStatus.text = BatteryStatusFormatter.format(this, battery, isCharging)
            binding.tvBatteryStatus.visibility = View.VISIBLE
        }
    }

    private fun reconnectDevice() {
        BleOperateManager.getInstance()
            .connectDirectly(DeviceManager.getInstance().deviceAddress)
    }

    @BleIsConnected
    private fun disconnectDevice() {
        BleOperateManager.getInstance().disconnect()
    }

    @BleIsConnected
    private fun createBond() {
        BleOperateManager.getInstance().bleCreateBond()
    }

    @BleIsConnected
    private fun unbindDevice() {
        BleOperateManager.getInstance().unBindDevice()
    }

    @BleIsConnected
    private fun findDevice() {
        CommandHandle.getInstance().executeReqCmd(FindDeviceReq()) { }
    }

    override fun onDestroy() {
        super.onDestroy()
        BleOperateManager.getInstance().removeNotifyListener(MAIN_KEY)
    }
}

