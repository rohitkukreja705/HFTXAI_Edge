package com.qcwireless.sdksample.log

import android.os.Handler
import android.os.Looper
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.IdentityHashMap
import java.util.Locale
import java.util.concurrent.CopyOnWriteArraySet

object BluetoothLogManager {

    const val DEFAULT_MAX_LOG_LENGTH = 50_000

    private const val MAX_ITEM_LENGTH = 1600
    private const val MAX_DEPTH = 2
    private const val MAX_COLLECTION_ITEMS = 20
    private const val MAX_OBJECT_FIELDS = 20

    interface LogListener {
        fun onLogTextChanged(content: String)
    }

    @Volatile
    var maxLogLength: Int = DEFAULT_MAX_LOG_LENGTH
        set(value) {
            field = value.coerceAtLeast(1)
            trimIfNeeded()
        }

    @Volatile
    var isCollecting: Boolean = true

    @Volatile
    var showTimestamp: Boolean = true

    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = CopyOnWriteArraySet<LogListener>()
    private val lock = Any()
    private var logContent: String = ""
    private val timeFormat = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    }

    fun addRequestLog(owner: String, method: String, args: Array<Any?>?) {
        val businessArgs = args.orEmpty().filterNot { it == null || isNonConcreteCallbackArg(it) }
        val commandName = businessArgs
            .firstOrNull()
            ?.javaClass
            ?.simpleName
            ?.takeIf { it.isNotBlank() }
            ?: method
        val params = if (businessArgs.isEmpty()) {
            "[]"
        } else {
            businessArgs.toTypedArray().toPrettyString()
        }
        appendLine("[REQ]\ncmd=$commandName params=$params", withTimestamp = true)
    }

    fun addCallbackLog(owner: String, method: String, args: Array<Any?>?) {
        appendLine("[RSP]\nresult=${args.toPrettyString()}", withTimestamp = true)
    }

    fun addExternalLog(content: String) {
        appendLine(content, withTimestamp = true)
    }

    fun clear() {
        synchronized(lock) {
            logContent = ""
        }
        notifyListeners()
    }

    fun addListener(listener: LogListener) {
        listeners.add(listener)
        val snapshot = getLogContent()
        mainHandler.post {
            listener.onLogTextChanged(snapshot)
        }
    }

    fun removeListener(listener: LogListener) {
        listeners.remove(listener)
    }

    fun getLogContent(): String {
        synchronized(lock) {
            return logContent
        }
    }

    private fun appendLine(rawLine: String, withTimestamp: Boolean = true) {
        if (!isCollecting) {
            return
        }
        val safeLine = sanitizeLogEntry(rawLine)
        val line = if (withTimestamp && showTimestamp) {
            "${timeFormat.get().format(Date())} $safeLine"
        } else {
            safeLine
        }
        synchronized(lock) {
            val base = if (logContent.isNotEmpty() && !logContent.endsWith("\n")) {
                "$logContent\n"
            } else {
                logContent
            }
            val candidate = "$base$line\n"
            logContent = trimToLength(candidate)
        }
        notifyListeners()
    }

    private fun trimIfNeeded() {
        synchronized(lock) {
            logContent = trimToLength(logContent)
        }
        notifyListeners()
    }

    private fun trimToLength(content: String): String {
        if (content.length <= maxLogLength) {
            return content
        }
        val trimmed = content.takeLast(maxLogLength)
        val firstLineBreak = trimmed.indexOf('\n')
        return if (firstLineBreak in 0 until trimmed.lastIndex) {
            trimmed.substring(firstLineBreak + 1)
        } else {
            trimmed
        }
    }

    private fun notifyListeners() {
        val snapshot = getLogContent()
        mainHandler.post {
            listeners.forEach { it.onLogTextChanged(snapshot) }
        }
    }

    private fun isLikelyCallback(value: Any): Boolean {
        val className = value.javaClass.name
        return className.contains("Callback", ignoreCase = true) ||
            className.contains("Listener", ignoreCase = true)
    }

    private fun isNonConcreteCallbackArg(value: Any): Boolean {
        val clazz = value.javaClass
        if (isLikelyCallback(value)) {
            return true
        }
        if (Proxy.isProxyClass(clazz) || clazz.name.contains("\$Proxy")) {
            return true
        }
        if (clazz.name.contains("InvocationHandler", ignoreCase = true)) {
            return true
        }
        return clazz.interfaces.any {
            it.name.startsWith("com.oudmon.ble.base.communication") && isLikelyCallbackName(it.simpleName)
        }
    }

    private fun isLikelyCallbackName(simpleName: String): Boolean {
        return simpleName.contains("Callback") ||
            simpleName.contains("Response") ||
            simpleName.contains("Listener") ||
            simpleName == "ICallback"
    }

    private fun Array<Any?>?.toPrettyString(): String {
        if (this.isNullOrEmpty()) {
            return "[]"
        }
        val serializer = ValueSerializer()
        return this.joinToString(prefix = "[", postfix = "]") { serializer.format(it) }
    }

    private fun sanitizeInline(text: String): String {
        return text
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t")
    }

    private fun sanitizeLogEntry(text: String): String {
        return text
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun clip(text: String): String {
        return if (text.length > MAX_ITEM_LENGTH) {
            text.substring(0, MAX_ITEM_LENGTH) + "...(trimmed)"
        } else {
            text
        }
    }

    private class ValueSerializer {
        private val visited = Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())

        fun format(value: Any?, depth: Int = 0): String {
            return try {
                BluetoothLogManager.clip(formatInternal(value, depth))
            } catch (_: Throwable) {
                "<serialize-error>"
            }
        }

        private fun formatInternal(value: Any?, depth: Int): String {
            if (value == null) {
                return "null"
            }
            val clazz = value.javaClass

            if (value is String) {
                return "\"${BluetoothLogManager.sanitizeInline(value)}\""
            }
            if (value is Number || value is Boolean || value is Char) {
                return BluetoothLogManager.sanitizeInline(value.toString())
            }
            if (value is Enum<*>) {
                return "${simpleName(clazz)}.${value.name}"
            }

            if (clazz.isArray) {
                val size = java.lang.reflect.Array.getLength(value)
                val limit = minOf(size, MAX_COLLECTION_ITEMS)
                val items = (0 until limit).joinToString(", ") { index ->
                    formatInternal(java.lang.reflect.Array.get(value, index), depth + 1)
                }
                val suffix = if (size > limit) ", ...(${size - limit} more)" else ""
                val componentName = simpleName(clazz.componentType ?: Any::class.java)
                return "$componentName[$size]($items$suffix)"
            }

            if (value is Collection<*>) {
                val size = value.size
                val limit = minOf(size, MAX_COLLECTION_ITEMS)
                val items = value.take(limit).joinToString(", ") { item ->
                    formatInternal(item, depth + 1)
                }
                val suffix = if (size > limit) ", ...(${size - limit} more)" else ""
                return "${simpleName(clazz)}(size=$size)[$items$suffix]"
            }

            if (value is Map<*, *>) {
                val size = value.size
                val entries = value.entries.take(MAX_COLLECTION_ITEMS).joinToString(", ") { entry ->
                    "${formatInternal(entry.key, depth + 1)}=${formatInternal(entry.value, depth + 1)}"
                }
                val suffix = if (size > MAX_COLLECTION_ITEMS) ", ...(${size - MAX_COLLECTION_ITEMS} more)" else ""
                return "${simpleName(clazz)}(size=$size){$entries$suffix}"
            }

            if (depth >= MAX_DEPTH) {
                return "${simpleName(clazz)}{...}"
            }
            if (!visited.add(value)) {
                return "${simpleName(clazz)}{...cycle...}"
            }

            return try {
                val fields = collectFields(clazz)
                if (fields.isEmpty()) {
                    fallbackText(value, clazz)
                } else {
                    val parts = mutableListOf<String>()
                    val limit = minOf(fields.size, MAX_OBJECT_FIELDS)
                    for (index in 0 until limit) {
                        val field = fields[index]
                        val fieldValue = runCatching {
                            field.isAccessible = true
                            field.get(value)
                        }.getOrElse { "<inaccessible>" }
                        parts.add("${field.name}=${formatInternal(fieldValue, depth + 1)}")
                    }
                    val suffix = if (fields.size > limit) ", ...(${fields.size - limit} more fields)" else ""
                    "${simpleName(clazz)}{${parts.joinToString(", ")}$suffix}"
                }
            } finally {
                visited.remove(value)
            }
        }

        private fun collectFields(clazz: Class<*>): List<java.lang.reflect.Field> {
            val fields = mutableListOf<java.lang.reflect.Field>()
            var current: Class<*>? = clazz
            while (current != null && current != Any::class.java) {
                current.declaredFields
                    .filter { field ->
                        !field.isSynthetic && !Modifier.isStatic(field.modifiers)
                    }
                    .forEach { fields.add(it) }
                current = current.superclass
            }
            return fields
        }

        private fun fallbackText(value: Any, clazz: Class<*>): String {
            val text = BluetoothLogManager.sanitizeInline(value.toString())
            return if (DEFAULT_OBJECT_PATTERN.matches(text)) {
                "${simpleName(clazz)}{}"
            } else {
                text
            }
        }

        private fun simpleName(clazz: Class<*>): String {
            return clazz.simpleName.takeIf { it.isNotBlank() } ?: clazz.name.substringAfterLast('.')
        }

        companion object {
            private val DEFAULT_OBJECT_PATTERN = Regex("^[\\w.$]+@[0-9a-fA-F]+$")
        }
    }
}
