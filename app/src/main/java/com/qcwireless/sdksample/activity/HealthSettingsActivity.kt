package com.qcwireless.sdksample.activity

import android.os.Bundle
import android.util.Log
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.task.BleTaskCategory
import com.oudmon.ble.base.bluetooth.task.BleTaskError
import com.oudmon.ble.base.communication.DfuHandle
import com.oudmon.ble.base.communication.ICommandResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.bean.IntervalHeartRateEntity
import com.oudmon.ble.base.communication.bigData.resp.IIntervalHeartRateCallback
import com.oudmon.ble.base.communication.dfu_temperature.TemperatureOnceEntity
import com.oudmon.ble.base.communication.entity.BlePressure
import com.oudmon.ble.base.communication.entity.BleStepDetails
import com.oudmon.ble.base.communication.file.FileHandle
import com.oudmon.ble.base.communication.file.SimpleCallback
import com.oudmon.ble.base.communication.req.HRVReq
import com.oudmon.ble.base.communication.req.PressureReq
import com.oudmon.ble.base.communication.req.ReadDetailSportDataReq
import com.oudmon.ble.base.communication.req.ReadHeartRateReq
import com.oudmon.ble.base.communication.req.ReadPressureReq
import com.oudmon.ble.base.communication.req.StartHeartRateReq
import com.oudmon.ble.base.communication.req.StopHeartRateReq
import com.oudmon.ble.base.communication.rsp.HRVRsp
import com.oudmon.ble.base.communication.rsp.PressureRsp
import com.oudmon.ble.base.communication.rsp.ReadBlePressureRsp
import com.oudmon.ble.base.communication.rsp.ReadDetailSportDataRsp
import com.oudmon.ble.base.communication.rsp.ReadHeartRateRsp
import com.oudmon.ble.base.communication.rsp.StopHeartRateRsp
import com.oudmon.ble.base.util.DateUtil
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.databinding.ActivityHealthSettingsBinding
import com.qcwireless.sdksample.ext.startKtxActivity
import com.qcwireless.sdksample.log.BluetoothLogManager

class HealthSettingsActivity : BaseFunctionActivity() {
    private lateinit var binding: ActivityHealthSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setupViews() {
        super.setupViews()
        binding.titleBar.tvTitle.text = getString(R.string.title_health_settings)
        binding.fivBatchSync.setOnClickListener { syncHealthDataInBatch() }
        binding.fivSteps.setOnClickListener { startKtxActivity<StepsActivity>() }
        binding.fivSleep.setOnClickListener { startKtxActivity<SleepActivity>() }
        binding.fivSportRecord.setOnClickListener { startKtxActivity<SportRecordActivity>() }
        binding.fivHeartRate.setOnClickListener { startKtxActivity<HeartRateActivity>() }
        binding.fivBloodPressure.setOnClickListener { startKtxActivity<BloodPressureActivity>() }
        binding.fivBloodOxygen.setOnClickListener { startKtxActivity<BloodOxygenActivity>() }
        binding.fivHrv.setOnClickListener { startKtxActivity<HrvActivity>() }
        binding.fivPressure.setOnClickListener { startKtxActivity<PressureActivity>() }
        binding.fivTemperature.setOnClickListener { startKtxActivity<TemperatureActivity>() }
        binding.fivSugarLipids.setOnClickListener { startKtxActivity<SugarLipidsActivity>() }
        binding.fivDrinkReminder.setOnClickListener { startKtxActivity<DrinkReminderActivity>() }
        binding.fivSedentaryReminder.setOnClickListener { startKtxActivity<SedentaryReminderActivity>() }
        refreshCachesIfConnected()
    }

    @BleIsConnected
    private fun syncHealthDataInBatch() {
        val manager = BleOperateManager.getInstance()
        val fileHandle = FileHandle.getInstance()
        val dfuHandle = DfuHandle.getInstance()
        val stepDetailsRange =
            mutableListOf<BleOperateManager.DayIndexedData<List<BleStepDetails>>>()
        val heartRatesRange =
            mutableListOf<BleOperateManager.DayIndexedData<ReadHeartRateRsp>>()
        val bloodPressuresRange =
            mutableListOf<BleOperateManager.DayIndexedData<List<BlePressure>>>()
        val pressuresRange =
            mutableListOf<BleOperateManager.DayIndexedData<PressureRsp>>()
        val hrvsRange =
            mutableListOf<BleOperateManager.DayIndexedData<HRVRsp>>()
        //You can name the task name freely, and it can be used to obtain the result of the previous command
        //e.g. ReadDetailSportDataReq command can name "today_step_detail" or "step_detail_1",even be any string
        manager.newTaskChain()
            .command(
                "today_step_detail",
                ReadDetailSportDataReq(0, 0, 95),
                5_000L,
                ICommandResponse<ReadDetailSportDataRsp> { resultEntity ->
                    logBatchDetail("today_step_detail", resultEntity.bleStepDetailses)
                }
            )
            .command(
                "step_detail",
                ReadDetailSportDataReq(1, 0, 95),
                5_000L,
                ICommandResponse<ReadDetailSportDataRsp> { resultEntity ->
                    logBatchDetail("step_detail(1)", resultEntity.bleStepDetailses)
                }
            )
            .command(
                "step_details_0",
                ReadDetailSportDataReq(0, 0, 95),
                5_000L,
                ICommandResponse<ReadDetailSportDataRsp> { resultEntity -> stepDetailsRange += BleOperateManager.DayIndexedData(0, resultEntity.bleStepDetailses) }
            )
            .command(
                "step_details_1",
                ReadDetailSportDataReq(1, 0, 95),
                5_000L,
                ICommandResponse<ReadDetailSportDataRsp> { resultEntity -> stepDetailsRange += BleOperateManager.DayIndexedData(1, resultEntity.bleStepDetailses) }
            )
            .command(
                "step_details_2",
                ReadDetailSportDataReq(2, 0, 95),
                5_000L,
                ICommandResponse<ReadDetailSportDataRsp> { resultEntity ->
                    stepDetailsRange += BleOperateManager.DayIndexedData(2, resultEntity.bleStepDetailses)
                    logBatchDetail("step_details(0,3)", stepDetailsRange)
                }
            )
            .command(
                "today_heart_rate",
                ReadHeartRateReq(dayStartSeconds(0)),
                5_000L,
                ICommandResponse<ReadHeartRateRsp> { resultEntity ->
                    logBatchDetail("today_heart_rate", resultEntity)
                }
            )
            .command(
                "heart_rate",
                ReadHeartRateReq(dayStartSeconds(1)),
                5_000L,
                ICommandResponse<ReadHeartRateRsp> { resultEntity ->
                    logBatchDetail("heart_rate(1)", resultEntity)
                }
            )
            .command(
                "heart_rates_0",
                ReadHeartRateReq(dayStartSeconds(0)),
                5_000L,
                ICommandResponse<ReadHeartRateRsp> { resultEntity -> heartRatesRange += BleOperateManager.DayIndexedData(0, resultEntity) }
            )
            .command(
                "heart_rates_1",
                ReadHeartRateReq(dayStartSeconds(1)),
                5_000L,
                ICommandResponse<ReadHeartRateRsp> { resultEntity -> heartRatesRange += BleOperateManager.DayIndexedData(1, resultEntity) }
            )
            .command(
                "heart_rates_2",
                ReadHeartRateReq(dayStartSeconds(2)),
                5_000L,
                ICommandResponse<ReadHeartRateRsp> { resultEntity ->
                    heartRatesRange += BleOperateManager.DayIndexedData(2, resultEntity)
                    logBatchDetail("heart_rates(0,3)", heartRatesRange)
                }
            )
//            .command(
//                "today_blood_pressure",
//                ReadPressureReq(dayStartSeconds(0)),
//                5_000L,
//                ICommandResponse<ReadBlePressureRsp> { resultEntity ->
//                    logBatchDetail("today_blood_pressure", resultEntity.valueList)
//                }
//            )
//            .command(
//                "blood_pressure",
//                ReadPressureReq(dayStartSeconds(1)),
//                5_000L,
//                ICommandResponse<ReadBlePressureRsp> { resultEntity ->
//                    logBatchDetail("blood_pressure(1)", resultEntity.valueList)
//                }
//            )
//            .command(
//                "blood_pressures_0",
//                ReadPressureReq(dayStartSeconds(0)),
//                5_000L,
//                ICommandResponse<ReadBlePressureRsp> { resultEntity -> bloodPressuresRange += BleOperateManager.DayIndexedData(0, resultEntity.valueList) }
//            )
//            .command(
//                "blood_pressures_1",
//                ReadPressureReq(dayStartSeconds(1)),
//                5_000L,
//                ICommandResponse<ReadBlePressureRsp> { resultEntity -> bloodPressuresRange += BleOperateManager.DayIndexedData(1, resultEntity.valueList) }
//            )
//            .command(
//                "blood_pressures_2",
//                ReadPressureReq(dayStartSeconds(2)),
//                5_000L,
//                ICommandResponse<ReadBlePressureRsp> { resultEntity ->
//                    bloodPressuresRange += BleOperateManager.DayIndexedData(2, resultEntity.valueList)
//                    logBatchDetail("blood_pressures(0,3)", bloodPressuresRange)
//                }
//            )
            .command(
                "today_pressure",
                PressureReq(0),
                5_000L,
                ICommandResponse<PressureRsp> { resultEntity ->
                    logBatchDetail("today_pressure", resultEntity)
                }
            )
            .command(
                "pressure",
                PressureReq(1),
                5_000L,
                ICommandResponse<PressureRsp> { resultEntity ->
                    logBatchDetail("pressure(1)", resultEntity)
                }
            )
            .command(
                "pressures_0",
                PressureReq(0),
                5_000L,
                ICommandResponse<PressureRsp> { resultEntity -> pressuresRange += BleOperateManager.DayIndexedData(0, resultEntity) }
            )
            .command(
                "pressures_1",
                PressureReq(1),
                5_000L,
                ICommandResponse<PressureRsp> { resultEntity -> pressuresRange += BleOperateManager.DayIndexedData(1, resultEntity) }
            )
            .command(
                "pressures_2",
                PressureReq(2),
                5_000L,
                ICommandResponse<PressureRsp> { resultEntity ->
                    pressuresRange += BleOperateManager.DayIndexedData(2, resultEntity)
                    logBatchDetail("pressures(0,3)", pressuresRange)
                }
            )
            .command(
                "today_hrv",
                HRVReq(0),
                5_000L,
                ICommandResponse<HRVRsp> { resultEntity ->
                    logBatchDetail("today_hrv", resultEntity)
                }
            )
            .command(
                "hrv",
                HRVReq(1),
                5_000L,
                ICommandResponse<HRVRsp> { resultEntity ->
                    logBatchDetail("hrv(1)", resultEntity)
                }
            )
            .command(
                "hrvs_0",
                HRVReq(0),
                5_000L,
                ICommandResponse<HRVRsp> { resultEntity -> hrvsRange += BleOperateManager.DayIndexedData(0, resultEntity) }
            )
            .command(
                "hrvs_1",
                HRVReq(1),
                5_000L,
                ICommandResponse<HRVRsp> { resultEntity -> hrvsRange += BleOperateManager.DayIndexedData(1, resultEntity) }
            )
            .command(
                "hrvs_2",
                HRVReq(2),
                5_000L,
                ICommandResponse<HRVRsp> { resultEntity ->
                    hrvsRange += BleOperateManager.DayIndexedData(2, resultEntity)
                    logBatchDetail("hrvs(0,3)", hrvsRange)
                }
            ).largeData("interval_heart_rate_0", 30_000L) { task ->
                LargeDataHandler.getInstance().syncIntervalHeartRateWithCallback(
                    0,
                    object : IIntervalHeartRateCallback {
                        override fun readIntervalHeartRate(data: IntervalHeartRateEntity?) {
                            if (!task.isActive) {
                                return
                            }
                            logBatchDetail("interval_heart_rate_0", data)
                            task.success(data)
                        }
                    }
                )
            }
//            .file("temp_once_0", 30_000L) { task ->
//                val callback = object : SimpleCallback() {
//                    override fun onUpdateTemperatureList(array: MutableList<TemperatureOnceEntity>?) {
//                        if (!task.isActive) {
//                            return
//                        }
//                        logBatchDetail("temp_once_0", array)
//                        task.success(array)
//                    }
//
//                    override fun onActionResult(type: Int, errCode: Int) {
//                        if (!task.isActive || errCode == 0) {
//                            return
//                        }
//                        Log.e("HealthTTest", "temp_once_0 failed type=$type errCode=$errCode")
//                        task.fail(errCode, "file action failed")
//                    }
//                }
//                fileHandle.clearCallback()
//                fileHandle.registerCallback(callback)
//                fileHandle.initRegister(task.id())
//                task.addCleanup {
//                    fileHandle.clearCallback()
//                    fileHandle.endAndRelease(task.id())
//                }
//                fileHandle.startObtainTemperatureOnce(0)
//            }
//            .measurement("one_click_measurement", 40_000L) { task ->
//                val commandSent = task.executeCommand(
//                    StartHeartRateReq.getSimpleReq(StartHeartRateReq.TYPE_One_lick_Measurement),
//                    ICommandResponse<StopHeartRateRsp> { resultEntity ->
//                        if (!task.isActive) {
//                            return@ICommandResponse
//                        }
//                        if (resultEntity.errCode.toInt() != 0) {
//                            Log.e(
//                                "HealthTTest",
//                                "one_click_measurement failed errCode=${resultEntity.errCode}"
//                            )
//                            task.fail(resultEntity.errCode.toInt(), "one click measurement failed")
//                            return@ICommandResponse
//                        }
//                        Log.i("HealthTTest", "one_click_measurement rsp = $resultEntity")
//                        if (resultEntity.heart > 0 || resultEntity.hrv > 0 || resultEntity.bloodOxygen > 0) {
//                            task.executeCommandNoCallback(StopHeartRateReq.stopOneClickMeasurement())
//                            logBatchDetail("one_click_measurement", resultEntity)
//                            task.success(resultEntity)
//                        }
//                    }
//                )
//                if (!commandSent) {
//                    Log.e("HealthTTest", "one_click_measurement failed: command write failed")
//                    task.fail(BleTaskError.ERR_DISCONNECTED, "command write failed")
//                }
//            }
            .task("post_sync_summary", BleTaskCategory.SYSTEM, 3_000L) { task ->
                val summary = "health, large data, file and measurement steps completed"
                Log.i("HealthTTest", "post_sync_summary success = $summary")
                task.success(summary)
            }
//            .dfu("dfu_start", 30_000L) { task ->
//                if (!dfuHandle.checkFile(firmwarePath)) {
//                    Log.e("HealthTTest", "dfu_start failed: invalid firmware file = $firmwarePath")
//                    task.fail(-1, "invalid firmware file")
//                    return@dfu
//                }
//                dfuHandle.start(object : DfuHandle.IOpResult {
//                    override fun onActionResult(type: Int, errCode: Int) {
//                        if (!task.isActive) {
//                            return
//                        }
//                        if (errCode == DfuHandle.RSP_OK) {
//                            Log.i("HealthTTest", "dfu_start success type=$type")
//                            task.success(type)
//                        } else {
//                            Log.e("HealthTTest", "dfu_start failed type=$type errCode=$errCode")
//                            task.fail(errCode, "dfu action failed")
//                        }
//                    }
//
//                    override fun onProgress(percent: Int) {
//                        Log.i("HealthTTest", "dfu_start progress=$percent")
//                    }
//                })
//            }
            .start(object : BleOperateManager.TaskChainListener {
                override fun onStepStart(index: Int, stepName: String?) {
                    Log.i("HealthTTest", "Step start: $index / $stepName")
                }

                override fun onStepSuccess(index: Int, stepName: String?, data: Any?) {
                    logBatchDetail("Step success: $index / $stepName", data)
                }

                override fun onStepFail(
                    index: Int,
                    stepName: String?,
                    errorCode: Int,
                    errorMsg: String?
                ) {
                    Log.e(
                        "HealthTTest",
                        "Step fail: $index / $stepName code=$errorCode msg=$errorMsg"
                    )
                }

                override fun onChainFinish(success: Boolean, cancelled: Boolean) {
                    Log.i("HealthTTest", "The entire chain ends success=$success cancelled=$cancelled")
                }
            })

    }

    private fun logBatchDetail(name: String, data: Any?) {
        val detail = HealthDataValueFormatter.formatFull(data)
        logLongInfo(LOG_TAG, "$name = $detail")
        BluetoothLogManager.addExternalLog("[BATCH]\nname=$name result=$detail")
    }

    private fun logLongInfo(tag: String, message: String) {
        if (message.length <= LOG_CHUNK_SIZE) {
            Log.i(tag, message)
            return
        }
        message.chunked(LOG_CHUNK_SIZE).forEachIndexed { index, chunk ->
            Log.i(tag, "${index + 1}/${(message.length + LOG_CHUNK_SIZE - 1) / LOG_CHUNK_SIZE} $chunk")
        }
    }

    private fun dayStartSeconds(dayIndex: Int): Long {
        val dateUtil = DateUtil()
        dateUtil.addDay(-dayIndex)
        return dateUtil.zeroTime
    }
    companion object {
        private const val LOG_TAG = "HealthBatchSync"
        private const val LOG_CHUNK_SIZE = 3_500
    }
}
