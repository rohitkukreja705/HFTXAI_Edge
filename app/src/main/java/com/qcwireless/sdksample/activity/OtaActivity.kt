package com.qcwireless.sdksample.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.DfuHandle
import com.qcwireless.sdksample.utils.GetFilePathFromUri
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.databinding.ActivityOtaBinding
import com.qcwireless.sdksample.ext.gone
import com.qcwireless.sdksample.ext.showToast
import com.qcwireless.sdksample.app.MyApplication


class OtaActivity : BaseActivity() {
    val REQUESTCODE_FROM_ACTIVITY = 1000
    private lateinit var binding: ActivityOtaBinding
    private var path = ""
    private lateinit var dfuHandle: DfuHandle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }


    private fun initView() {
        binding.run {
            tvProgress.text = getString(R.string.qc_text_0032, "0")
            selectFilePath.text = getString(R.string.qc_text_0033, "")
            dfuHandle = DfuHandle.getInstance()

            selectFile.setOnClickListener {
//                    FileSelector.from(this@OtaActivity)
//                        // .onlyShowFolder()  //只显示文件夹
//                        //.onlySelectFolder()  //只能选择文件夹
////                         .isSingle // 只能选择一个
////                        .setMaxCount(5) //设置最大选择数
//                        .setFileTypes( "bin") //设置文件类型
//                        .setSortType(FileSelector.BY_NAME_ASC) //设置名字排序
//                        //.setSortType(FileSelector.BY_TIME_ASC) //设置时间排序
//                        //.setSortType(FileSelector.BY_SIZE_DESC) //设置大小排序
//                        //.setSortType(FileSelector.BY_EXTENSION_DESC) //设置类型排序
//                        .requestCode(1) //设置返回码
////                        .setTargetPath("/storage/emulated/0/") //设置默认目录
//                        .start();
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, 0)
            }

            startOta.setOnClickListener {
                Log.d("OTATTest", "startOta:")
                XLog.tag("OTATTest").d("startOta:")
                if (path.isEmpty()) {
                    startOta.visibility = View.VISIBLE
                    XLog.tag("OTATTest").d("startOta:" + getString(R.string.qc_text_0035))
                    getString(R.string.qc_text_0035).showToast()
                    return@setOnClickListener
                }
                //dfu 升级实例
                //初始化回调
                dfuHandle.initCallback()
                //DFU 文件校验,path 固件文件路径
                if (dfuHandle.checkFile(path)) {
                    startOta.visibility = View.GONE
                    val forceUpdate = binding.forceUpdate.isChecked
                    dfuHandle.start(dfuOpResult, forceUpdate)
                } else {
                    startOta.visibility = View.VISIBLE
                    getString(R.string.qc_text_0034, path).showToast()
                }
            }

            stopOta.setOnClickListener {
                dfuHandle.endAndRelease()
                binding.startOta.visibility = View.VISIBLE
            }


        }
    }

    //dfuOpResult 回调说明
    private val dfuOpResult: DfuHandle.IOpResult = object : DfuHandle.IOpResult {
        override fun onActionResult(type: Int, errCode: Int) {
            if (errCode == DfuHandle.RSP_OK) {
                when (type) {
                    1 -> dfuHandle.init()
                    2 -> dfuHandle.sendPacket()
                    3 -> dfuHandle.check()
                    4 -> {
                        //升级成功,等待设备重启
                        dfuHandle.endAndRelease()
                        runOnUiThread {
                            binding.startOta.visibility = View.VISIBLE
                            getString(R.string.qc_text_0106).showToast()
                        }
                    }
                }
            } else {
                //升级异常或者失败
                runOnUiThread {
                    binding.startOta.visibility = View.VISIBLE
                    getString(R.string.qc_text_0107, errCode).showToast()
                }
            }
            XLog.tag("OTATTest").d("升级结果:type:$type,errCode:$errCode")
        }

        override fun onProgress(percent: Int) {
            //文件升级进度
            runOnUiThread {
                binding.tvProgress.text = getString(R.string.qc_text_0032, percent.toString())
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                path = GetFilePathFromUri.getFileAbsolutePath(this, uri)
                if (path.endsWith(".bin")) {
                    binding.tvTip.gone()
                    binding.selectFilePath.text = path
                }else{
                   getString(R.string.qc_text_0038).showToast()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 显示当前固件版本号
        val firmwareVersion = MyApplication.getInstance.firmwareVersion
        binding.tvFirmwareVersion.text = getString(R.string.qc_text_0104, firmwareVersion.ifEmpty { "" })
    }

    override fun onDestroy() {
        super.onDestroy()
        BleOperateManager.getInstance().unBindDevice()
    }

}
