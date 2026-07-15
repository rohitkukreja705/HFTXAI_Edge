package com.qcwireless.sdksample.cache

import com.oudmon.ble.base.communication.rsp.SetTimeRsp

/**
 * Cache for SetTimeRsp.
 */
class SetTime {
    companion object {
        val instance by lazy { SetTime() }
    }

    var supportTemperature by BooleanPreference(false)
    var supportPlate by BooleanPreference(false)
    var supportMenstruation by BooleanPreference(false)
    var supportCustomWallpaper by BooleanPreference(false)
    var supportBloodOxygen by BooleanPreference(false)
    var supportBloodPressure by BooleanPreference(false)
    var supportFeature by BooleanPreference(false)
    var supportOneKeyCheck by BooleanPreference(false)
    var supportWeather by BooleanPreference(false)
    var newSleepProtocol by BooleanPreference(false)
    var maxWatchFace by IntPreference(0)
    var supportContact by BooleanPreference(false)
    var supportManualHeart by BooleanPreference(false)
    var supportECard by BooleanPreference(false)
    var supportLocation by BooleanPreference(false)
    var maxContacts by IntPreference(0)
    var musicSupport by BooleanPreference(false)
    var rtkMcu by BooleanPreference(false)
    var ebookSupport by BooleanPreference(false)
    var supportWeChat by BooleanPreference(false)
    var supportHrv by BooleanPreference(false)
    var supportPressure by BooleanPreference(false)
    var supportManualBloodOxygen by BooleanPreference(false)
    var supportAppMeasure by BooleanPreference(false)

    fun updateFrom(rsp: SetTimeRsp) {
        supportTemperature = rsp.mSupportTemperature
        supportPlate = rsp.mSupportPlate
        supportMenstruation = rsp.mSupportMenstruation
        supportCustomWallpaper = rsp.mSupportCustomWallpaper
        supportBloodOxygen = rsp.mSupportBloodOxygen
        supportBloodPressure = rsp.mSupportBloodPressure
        supportFeature = rsp.mSupportFeature
        supportOneKeyCheck = rsp.mSupportOneKeyCheck
        supportWeather = rsp.mSupportWeather
        newSleepProtocol = rsp.mNewSleepProtocol
        maxWatchFace = rsp.mMaxWatchFace
        supportContact = rsp.mSupportContact
        supportManualHeart = rsp.mSupportManualHeart
        supportECard = rsp.mSupportECard
        supportLocation = rsp.mSupportLocation
        maxContacts = rsp.mMaxContacts
        musicSupport = rsp.mMusicSupport
        rtkMcu = rsp.rtkMcu
        ebookSupport = rsp.mEbookSupport
        supportWeChat = rsp.mSupportWeChat
        supportHrv = rsp.mSupportHrv
        supportPressure = rsp.mSupportPressure
        supportManualBloodOxygen = rsp.mSupportManualBloodOxygen
        supportAppMeasure = rsp.mSupportAppMeasure
    }
}
