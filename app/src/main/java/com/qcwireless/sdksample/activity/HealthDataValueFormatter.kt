package com.qcwireless.sdksample.activity

import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.util.Collections
import java.util.IdentityHashMap

object HealthDataValueFormatter {

    private const val MAX_ITEM_LENGTH = 1600
    private const val MAX_DEPTH = 2
    private const val MAX_COLLECTION_ITEMS = 20
    private const val MAX_OBJECT_FIELDS = 20
    private const val FULL_MAX_ITEM_LENGTH = 100_000
    private const val FULL_MAX_DEPTH = 6
    private const val FULL_MAX_COLLECTION_ITEMS = Int.MAX_VALUE
    private const val FULL_MAX_OBJECT_FIELDS = Int.MAX_VALUE
    private val defaultObjectPattern = Regex("^[\\w.$]+@[0-9a-fA-F]+$")

    fun format(value: Any?): String {
        return ValueSerializer().format(value)
    }

    fun formatFull(value: Any?): String {
        return ValueSerializer(
            maxItemLength = FULL_MAX_ITEM_LENGTH,
            maxDepth = FULL_MAX_DEPTH,
            maxCollectionItems = FULL_MAX_COLLECTION_ITEMS,
            maxObjectFields = FULL_MAX_OBJECT_FIELDS
        ).format(value)
    }

    private class ValueSerializer {
        private val maxItemLength: Int
        private val maxDepth: Int
        private val maxCollectionItems: Int
        private val maxObjectFields: Int
        private val visited = Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())

        constructor(
            maxItemLength: Int = MAX_ITEM_LENGTH,
            maxDepth: Int = MAX_DEPTH,
            maxCollectionItems: Int = MAX_COLLECTION_ITEMS,
            maxObjectFields: Int = MAX_OBJECT_FIELDS
        ) {
            this.maxItemLength = maxItemLength
            this.maxDepth = maxDepth
            this.maxCollectionItems = maxCollectionItems
            this.maxObjectFields = maxObjectFields
        }

        fun format(value: Any?, depth: Int = 0): String {
            return try {
                clip(formatInternal(value, depth))
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
                return "\"${sanitizeInline(value)}\""
            }
            if (value is Number || value is Boolean || value is Char) {
                return sanitizeInline(value.toString())
            }
            if (value is Enum<*>) {
                return "${simpleName(clazz)}.${value.name}"
            }
            if (Proxy.isProxyClass(clazz)) {
                return "${simpleName(clazz)}{}"
            }
            if (clazz.isArray) {
                val size = java.lang.reflect.Array.getLength(value)
                val limit = minOf(size, maxCollectionItems)
                val items = (0 until limit).joinToString(", ") { index ->
                    formatInternal(java.lang.reflect.Array.get(value, index), depth + 1)
                }
                val suffix = if (size > limit) ", ...(${size - limit} more)" else ""
                val componentName = simpleName(clazz.componentType ?: Any::class.java)
                return "$componentName[$size]($items$suffix)"
            }
            if (value is Collection<*>) {
                val size = value.size
                val limit = minOf(size, maxCollectionItems)
                val items = value.take(limit).joinToString(", ") { item ->
                    formatInternal(item, depth + 1)
                }
                val suffix = if (size > limit) ", ...(${size - limit} more)" else ""
                return "${simpleName(clazz)}(size=$size)[$items$suffix]"
            }
            if (value is Map<*, *>) {
                val size = value.size
                val entries = value.entries.take(maxCollectionItems).joinToString(", ") { entry ->
                    "${formatInternal(entry.key, depth + 1)}=${formatInternal(entry.value, depth + 1)}"
                }
                val suffix = if (size > maxCollectionItems) ", ...(${size - maxCollectionItems} more)" else ""
                return "${simpleName(clazz)}(size=$size){$entries$suffix}"
            }
            if (depth >= maxDepth) {
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
                    val limit = minOf(fields.size, maxObjectFields)
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
            val text = sanitizeInline(value.toString())
            return if (defaultObjectPattern.matches(text)) {
                "${simpleName(clazz)}{}"
            } else {
                text
            }
        }

        private fun simpleName(clazz: Class<*>): String {
            return clazz.simpleName.takeIf { it.isNotBlank() } ?: clazz.name.substringAfterLast('.')
        }

        private fun sanitizeInline(text: String): String {
            return text
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
        }

        private fun clip(text: String): String {
            return if (text.length > maxItemLength) {
                text.substring(0, maxItemLength) + "...(trimmed)"
            } else {
                text
            }
        }
    }
}
