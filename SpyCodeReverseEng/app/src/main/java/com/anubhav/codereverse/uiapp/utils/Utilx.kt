package com.anubhav.codereverse.uiapp.utils

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

internal object Utilx {

}

fun filterNotNullValues(map: Map<String, Any?>): Map<String, Any> {
    val result = LinkedHashMap<String, Any>()
    for ((key, value) in map) {
        if (value == null) continue
        if (value is String) {
            if (value.isNotEmpty()) result[key] = value
        } else {
            result[key] = value
        }
    }
    return result
}

fun filterValuesChange(newMap: Map<String, Any?>, oldMap: Map<String, Any?>): Map<String, Any?> {
    val result = LinkedHashMap<String, Any?>()
    newMap.keys.forEach { key ->
        if (newMap.getValue(key) != oldMap.getValue(key)) {
            result[key] = newMap.getValue(key)
        }
    }
    return result
}

fun <T : Any> toMap(obj: T): Map<String, Any?> {
    return (obj::class as KClass<T>).memberProperties.associate { prop ->
        prop.name to prop.get(obj)?.let { value ->
            if (value::class.isData) {
                toMap(value)
            } else {
                value
            }
        }
    }
}

fun <T : Any> toMapWithOnlyPrimaryConstructor(obj: T): Map<String, Any?> {
    val kClass = obj::class as KClass<T>
    val primaryConstructorPropertyNames =
        kClass.primaryConstructor?.parameters?.map { it.name } ?: run {
            return toMap(obj)
        }
    return kClass.memberProperties.mapNotNull { prop ->
        prop.name.takeIf { it in primaryConstructorPropertyNames }?.let {
            it to prop.get(obj)?.let { value ->
                if (value::class.isData) {
                    toMap(value)
                } else {
                    value
                }
            }
        }
    }.toMap()
}