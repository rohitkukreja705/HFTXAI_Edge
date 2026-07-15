package com.qcwireless.sdksample.annotation

/**
 * 蓝牙连接状态检查注解
 * 标注此注解的方法会在执行前检查蓝牙连接状态
 * 如果未连接则不执行方法并弹出提示
 * 
 * @param message 自定义提示消息，为空则使用默认提示
 * @param showToast 是否显示 Toast 提示，默认 true
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BleIsConnected(
    val message: String = "",
    val showToast: Boolean = true,
)