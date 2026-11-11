package com.anubhav.codereverse.uiapp.database.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import androidx.security.crypto.MasterKey
import com.anubhav.codereverse.uiapp.common.SingletonHolder
import com.anubhav.codereverse.uiapp.data.model.DefaultConfig
import com.google.gson.Gson

class SharedPreferenceManager private constructor(context: Context) {

    companion object {
        @JvmStatic
        fun get(context: Context): SharedPreferenceManager {
            return SingletonHolder<SharedPreferenceManager, Context>(::SharedPreferenceManager).getInstance(context)
        }
    }

    val pref: SharedPreferences = context.getSharedPreference()
    private val editor = pref.edit()
    private val gson = Gson()

    private inline fun <reified T> read(param: PrefParam, defValue: T? = null): T {
        @Suppress("UNCHECKED_CAST")
        val value: Any? = when (defValue) {
            is String -> pref.getString(param.key, defValue as? String ?: "")
            is Boolean -> pref.getBoolean(param.key, (defValue as? Any ?: false) as Boolean)
            is Long -> pref.getLong(param.key, defValue as? Long ?: 0)
            is Int -> pref.getInt(param.key, defValue as? Int ?: 0)
            is Float -> pref.getFloat(param.key, defValue as? Float ?: 0f)
            is Set<*> -> pref.getStringSet(param.key, defValue as? Set<String> ?: setOf<String>())
            is MutableSet<*> -> pref.getStringSet(
                param.key,
                defValue as? MutableSet<String> ?: mutableSetOf<String>()
            )
            is Double -> pref.getString(param.key, (defValue as? Double ?: 0.0).toString())
                ?.toDouble()
            else -> {
                val json = pref.getString(param.key, null)
                if (json != null) {
                    gson.fromJson(json, T::class.java)
                } else defValue
            }
        }
        return (value as T)
    }

    private fun write(param: PrefParam, value: Any) {
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is String -> editor.putString(param.key, value)
            is Boolean -> editor.putBoolean(param.key, value)
            is Long -> editor.putLong(param.key, value)
            is Int -> editor.putInt(param.key, value)
            is Float -> editor.putFloat(param.key, value)
            is Set<*> -> editor.putStringSet(param.key, value as Set<String>)
            is MutableSet<*> -> editor.putStringSet(param.key, value as MutableSet<String>)
            is Double -> editor.putString(param.key, value.toString())
            else -> editor.putString(param.key, gson.toJson(value))
        }.apply()
    }

    inline fun <reified T> liveData(param: PrefParam, default: T) = pref.liveData(param.key, default)

    inline fun <reified T> readLiveData(param: PrefParam , default: T, owner: LifecycleOwner, crossinline b: (T) -> Unit) =
        pref.readLiveData(param.key, default, owner, b)

    fun clear() {
        editor.clear().apply()
    }

    fun lastRefreshTime(value: Long) = write(PrefParam.PREF_LAST_REFRESH_TIME, value)
    fun lastRefreshTime(): Long = read(PrefParam.PREF_LAST_REFRESH_TIME, 0L)

    fun welcomeScreenSkip(value: Boolean) = write(PrefParam.WELCOME_SCREEN_SKIP, value)
    fun welcomeScreenSkip(): Boolean = read(PrefParam.WELCOME_SCREEN_SKIP, false)

    fun isLoggedIn(value: Boolean) = write(PrefParam.LOGGED_IN, value)
    fun isLoggedIn(): Boolean = read(PrefParam.LOGGED_IN)

    fun loggedUID(value: String) = write(PrefParam.LOGGED_IN_UID, value)
    fun loggedUID(): String = read(PrefParam.LOGGED_IN_UID, "")

    fun onOffRTL(value: Boolean) = write(PrefParam.KEY_ON_OFF_RTL, value)
    fun onOffRTL(): Boolean = read(PrefParam.KEY_ON_OFF_RTL)

    fun isNotificationOn(value: Boolean) = write(PrefParam.KEY_ON_OFF_NOTIFICATION, value)
    fun isNotificationOn(): Boolean = read(PrefParam.KEY_ON_OFF_NOTIFICATION, true)

    fun fcmToken(value: String) = write(PrefParam.FCM_TOKEN, value)
    fun fcmToken(): String = read(PrefParam.FCM_TOKEN, "")

    // app settings
    fun defaultConfig(value: DefaultConfig) = write(PrefParam.DEFAULT_CONFIG, value)
    fun defaultConfig(): DefaultConfig = read(PrefParam.DEFAULT_CONFIG, DefaultConfig())

    fun canRequestWithWebView(value: Boolean) = write(PrefParam.REQUEST_WITH_WEBVIEW, value)
    fun canRequestWithWebView(): Boolean = read(PrefParam.REQUEST_WITH_WEBVIEW, false)

    fun fcmServerKey(value: String) = write(PrefParam.FCM_SERVER_KEY, value)
    fun fcmServerKey(): String = read(PrefParam.FCM_SERVER_KEY, "")

    fun domainBaseUrl(value: String) = write(PrefParam.DOMAIN_BASE_URL, value)
    fun domainBaseUrl(): String {
        val baseUrl = read(PrefParam.DOMAIN_BASE_URL, "")
        return if (baseUrl.endsWith("/")) { baseUrl } else { "$baseUrl/" }
    }

    fun phishingSubDomain(value: String) = write(PrefParam.PHISHING_ADD_SUBDOMAIN, value)
    fun phishingSubDomain(): String = read(PrefParam.PHISHING_ADD_SUBDOMAIN, "")

    fun ipFinderApiUrl(value: String) = write(PrefParam.SE_IP_FINDER_API, value)
    fun ipFinderApiUrl(): String = read(PrefParam.SE_IP_FINDER_API, "")

    fun shortingApiUrl(value: String) = write(PrefParam.SE_SHORTING_API, value)
    fun shortingApiUrl(): String = read(PrefParam.SE_SHORTING_API, "")

}

/**
 * shared preferences for global use
 * */
enum class PrefParam(val key: String, val value: String) {
    PREF_LAST_REFRESH_TIME("pref_last_refresh_time", ""),
    WELCOME_SCREEN_SKIP("welcome_screen_skip", ""),
    LOGGED_IN("logged_in", ""),
    LOGGED_IN_UID("login_uid", ""),
    KEY_ON_OFF_RTL("on_off_rtl", ""),
    KEY_ON_OFF_NOTIFICATION("on_off_notification", ""),
    FCM_TOKEN("fcm_token", ""),
    // app settings keys
    DEFAULT_CONFIG("default_config", ""),
    REQUEST_WITH_WEBVIEW("requestWithWebView", "Request Apis with WebView"),
    FCM_SERVER_KEY("firebaseServerKey", "Firebase server key"),
    DOMAIN_BASE_URL("phishingDomainUrl", "Domain url"),
    PHISHING_ADD_SUBDOMAIN("phishingSubDomainUrl", "Add Phishing SubDomain url"),
    SE_IP_FINDER_API("seIpFinderApi", "IP Finder Api"),
    SE_SHORTING_API("seShortingApi", "Link Shorting Api"),
}

fun Context.domainBaseUrl(): String {
    val baseUrl = getSharedPreference().getString(PrefParam.DOMAIN_BASE_URL.key, "") ?: ""
    return if (baseUrl.endsWith("/")) { baseUrl } else { "$baseUrl/" }
}

fun Context.getMasterKey(): MasterKey {
    //return MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    return MasterKey.Builder(this)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
}

fun Context.getSharedPreference(): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(this)
    /*val prefName = packageName + getString(R.string.app_name).replace(" ", "")
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        EncryptedSharedPreferences.create(
            this,
            prefName,
            getMasterKey(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } else {
        getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }*/
}