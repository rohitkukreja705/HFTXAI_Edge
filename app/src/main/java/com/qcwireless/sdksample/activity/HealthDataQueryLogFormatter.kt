package com.qcwireless.sdksample.activity

import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.communication.bigData.bean.IntervalHeartRateEntity
import com.oudmon.ble.base.communication.bigData.bean.IntervalTemperatureEntity
import com.oudmon.ble.base.communication.rsp.ReadHeartRateRsp

object HealthDataQueryLogFormatter {

    fun formatRequest(
        title: String,
        scope: String,
        params: Map<String, Any?>
    ): String {
        return "[REQ]\nhealth=$title scope=$scope params=${formatParams(params)}"
    }

    fun formatSuccess(
        title: String,
        scope: String,
        data: Any?
    ): String {
        val result = formatHeartRateData(data)
            ?: formatTemperatureData(data)
            ?: HealthDataValueFormatter.format(data)
        return "[RSP]\nhealth=$title scope=$scope result=$result"
    }

    fun formatError(
        title: String,
        scope: String,
        code: Int,
        message: String?
    ): String {
        return "[RSP]\nhealth=$title scope=$scope errorCode=$code errorMsg=${message ?: "null"}"
    }

    private fun formatParams(params: Map<String, Any?>): String {
        return params.entries.joinToString(prefix = "{", postfix = "}") { entry ->
            "${entry.key}=${entry.value}"
        }
    }

    private fun formatHeartRateData(data: Any?): String? {
        return when (data) {
            is ReadHeartRateRsp -> {
                "\n${formatHeartRateDay(data)}"
            }
            is IntervalHeartRateEntity -> {
                "\n${formatIntervalHeartRateDay(data)}"
            }
            is Collection<*> -> {
                val dayData = data.mapNotNull { item ->
                    val dayIndexedData = item as? BleOperateManager.DayIndexedData<*> ?: return@mapNotNull null
                    when (val itemData = dayIndexedData.data) {
                        is ReadHeartRateRsp -> formatHeartRateDay(itemData, dayIndexedData.dayIndex)
                        is IntervalHeartRateEntity -> formatIntervalHeartRateDay(itemData, dayIndexedData.dayIndex)
                        else -> null
                    }
                }
                if (dayData.size == data.size && dayData.isNotEmpty()) {
                    "\n${dayData.joinToString("\n")}"
                } else {
                    null
                }
            }
            else -> null
        }
    }

    private fun formatHeartRateDay(
        data: ReadHeartRateRsp,
        dayIndex: Int? = null
    ): String {
        val prefix = if (dayIndex != null) {
            "dayIndex=$dayIndex "
        } else {
            ""
        }
        return "${prefix}utcTime=${data.getmUtcTime()} range=${data.getRange()} end=${data.isEndFlag()} " +
            "heartRates=${formatHeartRates(data.rawHeartRates())}"
    }

    private fun formatIntervalHeartRateDay(
        data: IntervalHeartRateEntity,
        dayIndex: Int = data.dayIndex
    ): String {
        return "dayIndex=$dayIndex interval=${data.interval} heartRates=${data.array.orEmpty().joinToString(prefix = "[", postfix = "]")}"
    }

    private fun formatHeartRates(values: ByteArray?): String {
        return (values ?: ByteArray(0))
            .joinToString(prefix = "[", postfix = "]") { value ->
                (value.toInt() and 0xFF).toString()
            }
    }

    private fun ReadHeartRateRsp.rawHeartRates(): ByteArray? {
        return runCatching {
            javaClass.getDeclaredField("mHeartRateArray").let { field ->
                field.isAccessible = true
                field.get(this) as? ByteArray
            }
        }.getOrNull() ?: getmHeartRateArray()
    }

    private fun formatTemperatureData(data: Any?): String? {
        return when (data) {
            is IntervalTemperatureEntity -> {
                "\n${formatTemperatureDay(data)}"
            }
            is Collection<*> -> {
                val dayData = data.mapNotNull { item ->
                    val dayIndexedData = item as? BleOperateManager.DayIndexedData<*> ?: return@mapNotNull null
                    val temperature = dayIndexedData.data as? IntervalTemperatureEntity ?: return@mapNotNull null
                    formatTemperatureDay(temperature, dayIndexedData.dayIndex)
                }
                if (dayData.size == data.size && dayData.isNotEmpty()) {
                    "\n${dayData.joinToString("\n")}"
                } else {
                    null
                }
            }
            else -> null
        }
    }

    private fun formatTemperatureDay(
        data: IntervalTemperatureEntity,
        dayIndex: Int = data.dayIndex
    ): String {
        val parts = mutableListOf<String>()
        val temperatures = data.array.orEmpty()
        val threeTemperatures = data.values.orEmpty()

        if (temperatures.isNotEmpty() || threeTemperatures.isEmpty()) {
            parts += "temperatures=${temperatures.joinToString(prefix = "[", postfix = "]")}"
        }
        if (threeTemperatures.isNotEmpty()) {
            parts += "threeTemperatures=${threeTemperatures.joinToString(prefix = "[", postfix = "]") { value ->
                "{value1=${value.value1}, value2=${value.value2}, value3=${value.value3}}"
            }}"
        }

        return "dayIndex=$dayIndex interval=${data.interval} code=${data.code} ${parts.joinToString(" ")}"
    }
}
