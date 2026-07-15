package com.qcwireless.sdksample.cache

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * MMKV filed Delegate
 */
abstract class MMKVDelegate<T>(protected val defaultValue: T) : ReadWriteProperty<Any?, T> {
    
    protected abstract fun put(key: String, value: T)
    protected abstract fun get(key: String): T
    
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return get(property.name)
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        put(property.name, value)
    }
}

/**
 * String Type Delegate
 */
class StringPreference(defaultValue: String = "") : MMKVDelegate<String>(defaultValue) {
    override fun put(key: String, value: String) {
        MMKVManager.putString(key, value)
    }
    
    override fun get(key: String): String {
        return MMKVManager.getString(key, defaultValue) ?: defaultValue
    }
}

/**
 * Int Type Delegate
 */
class IntPreference(defaultValue: Int = 0) : MMKVDelegate<Int>(defaultValue) {
    override fun put(key: String, value: Int) {
        MMKVManager.putInt(key, value)
    }
    
    override fun get(key: String): Int {
        return MMKVManager.getInt(key, defaultValue)
    }
}

/**
 * Long Type Delegate
 */
class LongPreference(defaultValue: Long = 0L) : MMKVDelegate<Long>(defaultValue) {
    override fun put(key: String, value: Long) {
        MMKVManager.putLong(key, value)
    }
    
    override fun get(key: String): Long {
        return MMKVManager.getLong(key, defaultValue)
    }
}

/**
 * Float Type Delegate
 */
class FloatPreference(defaultValue: Float = 0f) : MMKVDelegate<Float>(defaultValue) {
    override fun put(key: String, value: Float) {
        MMKVManager.putFloat(key, value)
    }
    
    override fun get(key: String): Float {
        return MMKVManager.getFloat(key, defaultValue)
    }
}

/**
 * Boolean Type Delegate
 */
class BooleanPreference(defaultValue: Boolean = false) : MMKVDelegate<Boolean>(defaultValue) {
    override fun put(key: String, value: Boolean) {
        MMKVManager.putBoolean(key, value)
    }
    
    override fun get(key: String): Boolean {
        return MMKVManager.getBoolean(key, defaultValue)
    }
}

/**
 * Double Type Delegate
 */
class DoublePreference(defaultValue: Double = 0.0) : MMKVDelegate<Double>(defaultValue) {
    override fun put(key: String, value: Double) {
        MMKVManager.putString(key, value.toString())
    }
    
    override fun get(key: String): Double {
        val stringValue = MMKVManager.getString(key, defaultValue.toString())
        return try {
            stringValue?.toDouble() ?: defaultValue
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }
}

/**
 * Extension function
 */
inline fun <reified T> mmkvPreference(defaultValue: T): ReadWriteProperty<Any?, T> = when (T::class) {
    String::class -> StringPreference(defaultValue as String) as ReadWriteProperty<Any?, T>
    Int::class -> IntPreference(defaultValue as Int) as ReadWriteProperty<Any?, T>
    Long::class -> LongPreference(defaultValue as Long) as ReadWriteProperty<Any?, T>
    Float::class -> FloatPreference(defaultValue as Float) as ReadWriteProperty<Any?, T>
    Boolean::class -> BooleanPreference(defaultValue as Boolean) as ReadWriteProperty<Any?, T>
    Double::class -> DoublePreference(defaultValue as Double) as ReadWriteProperty<Any?, T>
    else -> throw IllegalArgumentException("no such type: ${T::class}")
}