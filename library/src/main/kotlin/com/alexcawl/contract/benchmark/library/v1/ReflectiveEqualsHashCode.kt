package com.alexcawl.contract.benchmark.library.v1

import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

/**
 * V1 equality compares the captured fields of anonymous implementations of the same class.
 * The original algorithm is preserved, including treating two null reference fields as unequal.
 */
abstract class ReflectiveEqualsHashCode {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val clazz = javaClass
        if (clazz != other?.javaClass) return false
        return getDeclaredFields(clazz).all { field ->
            val type = field.type
            if (type.isPrimitive) {
                when {
                    type === java.lang.Boolean.TYPE -> field.getBoolean(this) == field.getBoolean(other)
                    type === java.lang.Character.TYPE -> field.getChar(this) == field.getChar(other)
                    type === java.lang.Byte.TYPE -> field.getByte(this) == field.getByte(other)
                    type === java.lang.Short.TYPE -> field.getShort(this) == field.getShort(other)
                    type === java.lang.Integer.TYPE -> field.getInt(this) == field.getInt(other)
                    type === java.lang.Long.TYPE -> field.getLong(this) == field.getLong(other)
                    type === java.lang.Float.TYPE -> field.getFloat(this) == field.getFloat(other)
                    type === java.lang.Double.TYPE -> field.getDouble(this) == field.getDouble(other)
                    else -> error("Invalid type")
                }
            } else {
                field.get(this)?.equals(field.get(other)) == true
            }
        }
    }

    override fun hashCode(): Int = getDeclaredFields(javaClass).fold(0) { acc, field ->
        31 * acc + (field.get(this)?.hashCode() ?: 0)
    }

    private fun getDeclaredFields(clazz: Class<*>): Array<out Field> =
        declaredFieldsCache.getOrPut(clazz) {
            clazz.declaredFields.onEach { it.isAccessible = true }
        }

    private companion object {
        private val declaredFieldsCache = ConcurrentHashMap<Class<*>, Array<out Field>>()
    }
}
