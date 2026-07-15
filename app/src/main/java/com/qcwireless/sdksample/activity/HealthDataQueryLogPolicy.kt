package com.qcwireless.sdksample.activity

object HealthDataQueryLogPolicy {

    private val skippedMethodNames = setOf(
        "getTodayStepDetail",
        "getStepDetail",
        "getStepDetails",
        "getTodaySleep",
        "getSleep",
        "getSleeps",
        "getTodayHeartRate",
        "getHeartRate",
        "getHeartRates",
        "getTodayBloodPressure",
        "getBloodPressure",
        "getBloodPressures",
        "getTodayBloodOxygen",
        "getBloodOxygen",
        "getBloodOxygens",
        "getTodayHrv",
        "getHrv",
        "getHrvs",
        "getTodayPressure",
        "getPressure",
        "getPressures",
        "getTodayTemperature",
        "getTemperature",
        "getTemperatures"
    )

    fun shouldSkipAutomaticSdkLogging(methodName: String): Boolean {
        return methodName in skippedMethodNames
    }
}
