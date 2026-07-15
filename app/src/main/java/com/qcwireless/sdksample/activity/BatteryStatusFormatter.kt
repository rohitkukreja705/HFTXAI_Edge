package com.qcwireless.sdksample.activity

import android.content.Context
import com.qcwireless.sdksample.R

object BatteryStatusFormatter {
    fun format(percentage: String, chargingText: String, isCharging: Boolean): String {
        return if (isCharging) {
            "$percentage $chargingText"
        } else {
            percentage
        }
    }

    fun format(context: Context, battery: Int, isCharging: Boolean): String {
        val percentage = "$battery%"
        return if (isCharging) {
            context.getString(R.string.qc_text_0102, percentage)
        } else {
            context.getString(R.string.qc_text_0105, percentage)
        }
    }
}
