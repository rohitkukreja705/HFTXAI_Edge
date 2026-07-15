package com.qcwireless.sdksample.activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.CommandHandle
import com.oudmon.ble.base.communication.Constants
import com.oudmon.ble.base.communication.req.SimpleKeyReq
import com.oudmon.ble.base.scan.BleScannerHelper
import com.oudmon.ble.base.scan.ScanRecord
import com.oudmon.ble.base.scan.ScanWrapperCallback
import com.qcwireless.sdksample.event.BluetoothEvent
import com.qcwireless.sdksample.utils.BluetoothUtils
import com.qcwireless.sdksample.adapter.DeviceListAdapter
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.bean.SmartWatch
import com.qcwireless.sdksample.databinding.ActivityDeviceBindBinding
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible
import com.qcwireless.sdksample.event.MessageEvent
import com.qcwireless.sdksample.ext.setOnClickListener
import com.qcwireless.sdksample.ext.showToast
import com.qcwireless.sdksample.utils.requestLocationPermission
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DeviceBindActivity : BaseActivity() {
    companion object {
        private const val CONNECT_TIMEOUT_MS = 60_000L
    }

    private lateinit var binding: ActivityDeviceBindBinding
    private lateinit var  adapter: DeviceListAdapter
    private var scanSize:Int=0
    private val runnable=MyRunnable()
    private val connectTimeoutRunnable = Runnable {
        handleConnectionResolution(connectionState.timeout())
    }

    private val myHandler : Handler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }

    private val connectionState = DeviceBindConnectionState()
    val deviceList = mutableListOf<SmartWatch>()
    private val allDeviceList = mutableListOf<SmartWatch>()
    val bleScanCallback: BleCallback = BleCallback()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDeviceBindBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        requestLocationPermission(this, PermissionCallback())
        binding.startScan.performClick()
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            return powerManager.isIgnoringBatteryOptimizations(this.packageName)
        }
        return false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        if(messageEvent is BluetoothEvent){
        Log.d("BleTTest", "onServiceDiscovered BLE_SERVICE_DISCOVERED:"+messageEvent.connect)
        handleConnectionResolution(connectionState.onConnectionEvent(messageEvent.connect))
        }
    }

    override fun setupViews() {
        super.setupViews()
        adapter = DeviceListAdapter(this, deviceList)
        binding.run {
            deviceRcv.layoutManager = LinearLayoutManager(this@DeviceBindActivity)
            deviceRcv.adapter = adapter
            titleBar.tvTitle.text=getString(R.string.text_1)
            titleBar.ivNavigateBefore.setOnClickListener {
                if (!handleBackNavigation()) {
                    finish()
                }
            }
            btnCancelConnect.setOnClickListener {
                cancelCurrentConnection(showCancelledToast = false)
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!handleBackNavigation()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        adapter.notifyDataSetChanged()

        adapter.run {
            setOnItemClickListener{ _, _, position->
                myHandler.removeCallbacks(runnable)
                val smartWatch: SmartWatch = deviceList[position]
                smartWatch.deviceAddress?.let {
                    startConnection(it)
                }
            }
        }

        binding.etFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterDeviceList(s?.toString() ?: "")
            }
        })

        setOnClickListener(binding.startScan) {
            deviceList.clear()
            allDeviceList.clear()
            adapter.notifyDataSetChanged()
            BleScannerHelper.getInstance().reSetCallback()
            if (!BluetoothUtils.isEnabledBluetooth(this@DeviceBindActivity)) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity!!.startActivityForResult(intent, 300)
            } else {
                scanSize = 0
                BleScannerHelper.getInstance()
                    .scanDevice(this@DeviceBindActivity, null, bleScanCallback)
                myHandler.removeCallbacks(runnable)
                myHandler.postDelayed(runnable, 15 * 1000)
            }
        }
    }

    inner class MyRunnable:Runnable{
        override fun run() {
            BleScannerHelper.getInstance().stopScan(this@DeviceBindActivity)
        }
    }

    private fun startConnection(deviceAddress: String) {
        if (!connectionState.start()) {
            return
        }
        BleScannerHelper.getInstance().stopScan(this)
        showConnectingOverlay()
        myHandler.removeCallbacks(connectTimeoutRunnable)
        myHandler.postDelayed(connectTimeoutRunnable, CONNECT_TIMEOUT_MS)
        BleOperateManager.getInstance().connectDirectly(deviceAddress)
    }

    private fun handleConnectionResolution(resolution: DeviceBindConnectionState.Resolution?) {
        when (resolution) {
            DeviceBindConnectionState.Resolution.SUCCESS -> {
                clearConnectingState()
                finish()
            }
            DeviceBindConnectionState.Resolution.FAILURE -> {
                clearConnectingState()
                getString(R.string.device_bind_connect_failed).showToast()
            }
            DeviceBindConnectionState.Resolution.TIMEOUT -> {
                clearConnectingState()
                BleOperateManager.getInstance().disconnect()
                getString(R.string.device_bind_connect_timeout).showToast()
            }
            DeviceBindConnectionState.Resolution.CANCELLED -> {
                clearConnectingState()
                BleOperateManager.getInstance().disconnect()
            }
            null -> Unit
        }
    }

    private fun cancelCurrentConnection(showCancelledToast: Boolean) {
        val resolution = connectionState.cancel() ?: return
        handleConnectionResolution(resolution)
        if (showCancelledToast) {
            getString(R.string.device_bind_connect_cancelled).showToast()
        }
    }

    private fun clearConnectingState() {
        myHandler.removeCallbacks(connectTimeoutRunnable)
        hideConnectingOverlay()
    }

    private fun showConnectingOverlay() {
        binding.connectingOverlay.isVisible = true
    }

    private fun hideConnectingOverlay() {
        binding.connectingOverlay.isVisible = false
    }

    private fun handleBackNavigation(): Boolean {
        if (!connectionState.isConnecting) {
            return false
        }
        cancelCurrentConnection(showCancelledToast = false)
        finish()
        return true
    }

    private fun filterDeviceList(filterText: String) {
        deviceList.clear()
        if (filterText.isEmpty()) {
            deviceList.addAll(allDeviceList)
        } else {
            val lowerFilter = filterText.lowercase()
            deviceList.addAll(allDeviceList.filter {
                it.deviceName.lowercase().contains(lowerFilter)
            })
        }
        adapter.notifyDataSetChanged()
    }



    override fun onDestroy() {
        myHandler.removeCallbacks(connectTimeoutRunnable)
        if (connectionState.isConnecting) {
            cancelCurrentConnection(showCancelledToast = false)
        }
        super.onDestroy()
        myHandler.postDelayed(Runnable {
            CommandHandle.getInstance()
                .executeReqCmd(SimpleKeyReq(Constants.CMD_BIND_SUCCESS),
                    null)
            BleScannerHelper.getInstance().stopScan(this@DeviceBindActivity)
        },1000)
    }



    inner class PermissionCallback : OnPermissionCallback {
        override fun onGranted(permissions: MutableList<String>, all: Boolean) {
            if (!all) {

            }
        }

        override fun onDenied(permissions: MutableList<String>, never: Boolean) {
            super.onDenied(permissions, never)
            if(never){
                XXPermissions.startPermissionActivity(this@DeviceBindActivity, permissions);
            }
        }

    }


    inner class BleCallback : ScanWrapperCallback {
        override fun onStart() {
        }

        override fun onStop() {

        }

        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
            if (device != null && (!device.name.isNullOrEmpty())) {
//                if (device.name.startsWith("O_")||device.name.startsWith("Q_")) {
//
//                }

                val smartWatch = SmartWatch(device.name, device.address, rssi)
                Log.i("1111",device.name+"---"+ device.address)

                if (!allDeviceList.contains(smartWatch)) {
                    scanSize++
                    allDeviceList.add(smartWatch)
                    allDeviceList.sortByDescending { it -> it.rssi }
                    filterDeviceList(binding.etFilter.text.toString())
                    if (scanSize > 30) {
                        BleScannerHelper.getInstance().stopScan(this@DeviceBindActivity)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {

        }

        override fun onParsedData(device: BluetoothDevice?, scanRecord: ScanRecord?) {

        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {

        }

    }
}
