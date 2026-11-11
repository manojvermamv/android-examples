package com.anubhav.codereverse.uiapp.database.preferences

import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.StringReader

abstract class SharedPreferenceLiveData<T>(private val sharedPrefs: SharedPreferences, val key: String, private val defValue: T) : LiveData<T>() {

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == this.key || key == null) {
            // Note that we get here on every preference write, even if the value has not changed
            updateIfChanged()
        }
    }

    abstract fun getValueFromPreferences(key: String, defValue: T): T

    override fun onActive() {
        super.onActive()
        updateIfChanged()
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }

    /** Update the live data value, but only if the value has changed. */
    private fun updateIfChanged() = with(getValueFromPreferences(key, defValue)) {
        if (value != this) value = this
    }
}

inline fun <reified T> SharedPreferences.liveData(key: String, default: T): SharedPreferenceLiveData<T> {
    @Suppress("UNCHECKED_CAST")
    return object : SharedPreferenceLiveData<T>(this, key, default) {
        override fun getValueFromPreferences(key: String, defValue: T): T {
            return when (default) {
                is String -> getString(key, default) as T
                is Int -> getInt(key, default) as T
                is Long -> getLong(key, default) as T
                is Boolean -> getBoolean(key, default) as T
                is Float -> getFloat(key, default) as T
                is Set<*> -> getStringSet(key, default as Set<String>) as T
                is MutableSet<*> -> getStringSet(key, default as MutableSet<String>) as T
                else -> throw IllegalArgumentException("generic type not handled")
            }
        }
    }
}

/**
 * Use Cases ->
 * SharedPreferences.readLiveData("MY_KEY", "defValue", lifecycleOwner, { value: String -> })
 * */

inline fun <reified T> SharedPreferences.readLiveData(key: String, default: T, owner: LifecycleOwner, crossinline b: (T) -> Unit) {
    var isCustomClz = false
    val defaultJson = "{\"key\": \"value\"}"

    @Suppress("UNCHECKED_CAST")
    val liveValues = object : SharedPreferenceLiveData<T>(this, key, default) {
        override fun getValueFromPreferences(key: String, defValue: T): T {
            return when (default) {
                is String -> getString(key, default) as T
                is Int -> getInt(key, default) as T
                is Long -> getLong(key, default) as T
                is Boolean -> getBoolean(key, default) as T
                is Float -> getFloat(key, default) as T
                is Set<*> -> getStringSet(key, default as Set<String>) as T
                is MutableSet<*> -> getStringSet(key, default as MutableSet<String>) as T
                else -> {
                    isCustomClz = true
                    getString(key, defaultJson) as T // just use for check to return default value (we can us for read Custom Data Class)
                }
            }
        }
    }

    liveValues.observe(owner, Observer {
        if (it != null) {
            var value = default
            if (isCustomClz && it is String) {
                if (it != defaultJson) {
                    val reader = JsonReader(StringReader(it))
                    reader.isLenient = true
                    value = Gson().fromJson(reader, T::class.java)
                }
            } else {
                value = it
            }
            if (value != null) b.invoke(value)
        }
    })
}