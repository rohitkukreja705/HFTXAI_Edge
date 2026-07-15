package com.qcwireless.sdksample.cache

/**
 * The functions supported by the device
 */
class DeviceFunctionSupport {
    companion object {
        val instance by lazy { DeviceFunctionSupport() }
    }

    var supportTouch by BooleanPreference(false)
    var supportMuslin by BooleanPreference(false)
    var supportAPPRevision by BooleanPreference(false)
    var supportBlePair by BooleanPreference(false)
    var supportGesture by BooleanPreference(false)
    var supportRingMusic by BooleanPreference(false)
    var supportRingVideo by BooleanPreference(false)
    var supportRingEbook by BooleanPreference(false)
    var supportCamera by BooleanPreference(false)
    var supportRingCall by BooleanPreference(false)
    var supportRingGame by BooleanPreference(false)
    var supportHeartMeasure by BooleanPreference(false)
    var supportLongSit by BooleanPreference(false)
    var supportDrink by BooleanPreference(false)
    var supportSkinTemperature by BooleanPreference(false)
    var supportNoSingleTemperature by BooleanPreference(false)
    var supportNotification by BooleanPreference(false)
    var supportCallReminder by BooleanPreference(false)
    var supportIntervalBloodOxygen by BooleanPreference(false)
    var supportIntervalTemperature by BooleanPreference(false)
    //Real Time Heart Rate
    var supportRealTimeHr by BooleanPreference(false)
    //Real Time Heart Rate Remind
    var supportRealTimeHrRemind by BooleanPreference(false)
    //Lover Interact
    var supportLoverInteract by BooleanPreference(false)
    var supportRt11 by BooleanPreference(false)

    fun updateFrom(rsp: com.oudmon.ble.base.communication.rsp.DeviceSupportFunctionRsp) {
        supportTouch = rsp.supportTouch
        supportMuslin = rsp.supportMoslin
        supportAPPRevision = rsp.supportAPPRevision
        supportBlePair = rsp.supportBlePair
        supportGesture = rsp.supportGesture
        supportRingMusic = rsp.supportRingMusic
        supportRingVideo = rsp.supportRingVideo
        supportRingEbook = rsp.supportRingEbook
        supportCamera = rsp.supportRingCamera
        supportRingCall = rsp.supportRingPhoneCall
        supportRingGame = rsp.supportRingGame
        supportHeartMeasure = rsp.supportHeart
        supportLongSit = rsp.supportLongSit
        supportDrink = rsp.supportDrink
        supportSkinTemperature = rsp.supportSkinTemperature
        supportNoSingleTemperature = rsp.supportNoSingleTemperature
        supportNotification = rsp.supportNotification
        supportCallReminder = rsp.supportCallReminder
        supportIntervalBloodOxygen = rsp.supportIntervalBloodOxygen
        supportIntervalTemperature = rsp.supportIntervalTemperature
        supportRealTimeHr = rsp.supportIntervalHeartRate
        supportRealTimeHrRemind = rsp.supportIntervalHeartRateRemind
        supportLoverInteract = rsp.supportLoverInteract
        supportRt11 = rsp.supportRt11
    }
}
