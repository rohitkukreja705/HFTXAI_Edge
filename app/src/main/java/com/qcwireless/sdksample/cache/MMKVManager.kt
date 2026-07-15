package com.qcwireless.sdksample.cache

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * MMKV Manager
 */
object MMKVManager {
    
    private var mmkv: MMKV? = null
    
    /**
     * init MMKV
     */
    fun initialize(context: Context) {
        MMKV.initialize(context)
        mmkv = MMKV.defaultMMKV()
    }
    
    /**
     * get MMKV instance
     */
    fun getInstance(): MMKV {
        return mmkv ?: throw IllegalStateException("MMKV is not init")
    }
    
    // String类型操作
    fun putString(key: String, value: String) {
        getInstance().encode(key, value)
    }
    
    fun getString(key: String, defaultValue: String): String? {
        return getInstance().decodeString(key, defaultValue)
    }
    
    // Int类型操作
    fun putInt(key: String, value: Int) {
        getInstance().encode(key, value)
    }
    
    fun getInt(key: String, defaultValue: Int): Int {
        return getInstance().decodeInt(key, defaultValue)
    }
    
    // Long类型操作
    fun putLong(key: String, value: Long) {
        getInstance().encode(key, value)
    }
    
    fun getLong(key: String, defaultValue: Long): Long {
        return getInstance().decodeLong(key, defaultValue)
    }
    
    // Float类型操作
    fun putFloat(key: String, value: Float) {
        getInstance().encode(key, value)
    }
    
    fun getFloat(key: String, defaultValue: Float): Float {
        return getInstance().decodeFloat(key, defaultValue)
    }
    
    // Boolean类型操作
    fun putBoolean(key: String, value: Boolean) {
        getInstance().encode(key, value)
    }
    
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getInstance().decodeBool(key, defaultValue)
    }
}