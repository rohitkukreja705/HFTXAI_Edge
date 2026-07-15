package com.qcwireless.sdksample.aspect

import android.bluetooth.BluetoothProfile
import com.elvishew.xlog.XLog
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.annotation.BleIsConnected
import com.qcwireless.sdksample.app.MyApplication
import com.qcwireless.sdksample.ext.showToast
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

@Aspect
class BleConnectionAspect {

    @Pointcut("execution(@com.qcwireless.sdksample.annotation.BleIsConnected * *(..)) && @annotation(annotation)")
    fun checkBleConnectionPoint(annotation: BleIsConnected) {
    }

    @Around("checkBleConnectionPoint(annotation)")
    fun aroundCheckBleConnection(
        joinPoint: ProceedingJoinPoint,
        annotation: BleIsConnected
    ): Any? {
        val manager = BleOperateManager.getInstance()
        val connected = manager.isConnected || manager.connectState == BluetoothProfile.STATE_CONNECTED
        XLog.tag("BleTTest").d("connect=$connected raw=${manager.isConnected} state=${manager.connectState}")
        if (!connected) {
            if (annotation.showToast) {
                var message = MyApplication.getApplication()?.getString(R.string.qc_text_0055)
                if (!annotation.message.isEmpty()) {
                    message = annotation.message
                }
                message?.showToast()
            }
            return null
        }
        return joinPoint.proceed()
    }

    private fun defaultReturnValue(returnType: Class<*>): Any? {
        return when (returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Byte.TYPE -> 0.toByte()
            java.lang.Short.TYPE -> 0.toShort()
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0f
            java.lang.Double.TYPE -> 0.0
            java.lang.Character.TYPE -> '\u0000'
            else -> null
        }
    }
}
