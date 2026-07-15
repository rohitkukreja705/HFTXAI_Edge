package com.qcwireless.sdksample.aspect

import com.qcwireless.sdksample.activity.HealthDataQueryLogPolicy
import com.qcwireless.sdksample.log.BluetoothLogManager
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@Aspect
class BluetoothLogAspect {

    @Pointcut(
        "(" +
            "call(* com.oudmon.ble.base.bluetooth.BleOperateManager.*(..)) || " +
            "call(* com.oudmon.ble.base.communication.CommandHandle.*(..)) || " +
            "call(* com.oudmon.ble.base.communication.LargeDataHandler.*(..)) || " +
            "call(* com.oudmon.ble.base.communication.file.FileHandle.*(..))" +
            ")" +
            " && !within(com.qcwireless.sdksample.aspect..*)"
    )
    fun bleSdkCall() {
    }

    @Around("bleSdkCall()")
    fun aroundBleSdkCall(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as? MethodSignature ?: return joinPoint.proceed()
        val methodName = signature.name
        val owner = signature.declaringType.simpleName

        if (HealthDataQueryLogPolicy.shouldSkipAutomaticSdkLogging(methodName)) {
            return joinPoint.proceed()
        }

        val args = joinPoint.args ?: emptyArray()
        val wrappedArgs = wrapCallbackArgs(owner, methodName, args)

        if (shouldLogRequest(methodName)) {
            BluetoothLogManager.addRequestLog(owner, methodName, wrappedArgs)
        }
        return if (wrappedArgs.isEmpty()) {
            joinPoint.proceed()
        } else {
            joinPoint.proceed(wrappedArgs)
        }
    }

    private fun shouldLogRequest(methodName: String): Boolean {
        if (methodName == "getInstance") {
            return false
        }
        if (methodName.startsWith("get")) {
            return false
        }
        return methodName != "hashCode" && methodName != "equals" && methodName != "toString"
    }

    private fun wrapCallbackArgs(
        owner: String,
        methodName: String,
        args: Array<Any?>
    ): Array<Any?> {
        if (args.isEmpty()) {
            return args
        }
        val wrapped = args.copyOf()
        for (index in wrapped.indices) {
            val arg = wrapped[index] ?: continue
            wrapped[index] = wrapIfCallback(owner, methodName, arg)
        }
        return wrapped
    }

    private fun wrapIfCallback(owner: String, requestMethod: String, original: Any): Any {
        if (isManagedProxy(original)) {
            return original
        }
        val callbackInterfaces = collectCallbackInterfaces(original.javaClass)
        if (callbackInterfaces.isEmpty()) {
            return original
        }
        return Proxy.newProxyInstance(
            original.javaClass.classLoader ?: javaClass.classLoader,
            callbackInterfaces.toTypedArray(),
            CallbackInvocationHandler(owner, requestMethod, original)
        )
    }

    private fun isManagedProxy(target: Any): Boolean {
        if (!Proxy.isProxyClass(target.javaClass)) {
            return false
        }
        return try {
            Proxy.getInvocationHandler(target) is CallbackInvocationHandler
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun collectCallbackInterfaces(clazz: Class<*>): Set<Class<*>> {
        val allInterfaces = linkedSetOf<Class<*>>()
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            current.interfaces.forEach { collectInterfaceTree(it, allInterfaces) }
            current = current.superclass
        }
        return allInterfaces.filterTo(linkedSetOf()) { isSdkCallbackInterface(it) }
    }

    private fun collectInterfaceTree(type: Class<*>, output: MutableSet<Class<*>>) {
        if (!output.add(type)) {
            return
        }
        type.interfaces.forEach { collectInterfaceTree(it, output) }
    }

    private fun isSdkCallbackInterface(type: Class<*>): Boolean {
        val name = type.name
        if (!name.startsWith("com.oudmon.ble.base.communication")) {
            return false
        }
        val simple = type.simpleName
        return simple.contains("Callback") ||
            simple.contains("Response") ||
            simple == "ICallback"
    }

    private class CallbackInvocationHandler(
        private val owner: String,
        private val requestMethod: String,
        private val target: Any
    ) : InvocationHandler {

        override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
            if (method.declaringClass == Any::class.java) {
                return invokeMethod(method, args)
            }

            BluetoothLogManager.addCallbackLog(
                owner,
                "$requestMethod.${method.name}",
                args
            )
            return invokeMethod(method, args)
        }

        private fun invokeMethod(method: Method, args: Array<out Any?>?): Any? {
            return try {
                method.invoke(target, *(args ?: emptyArray()))
            } catch (throwable: InvocationTargetException) {
                throw throwable.targetException
            }
        }
    }
}
