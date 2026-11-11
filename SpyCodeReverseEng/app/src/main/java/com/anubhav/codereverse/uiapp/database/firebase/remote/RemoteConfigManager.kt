package com.anubhav.codereverse.uiapp.database.firebase.remote

import com.anubhav.codereverse.uiapp.firebase.common.jsonToObjectOrNull
import com.anubhav.codereverse.uiapp.data.model.VipPaymentInfo
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor(private val gson: Gson) : IRemoteConfigApi {

//    Note :- in this example, I am using Dagger for dependence injection.
//    This class is annotated as Singleton hence config will be initialized at the app start time.
//    If you use another approach make sure to perform fetch and activate fetched values.

    private val config: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance().apply {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(CONFIG_CACHE_EXPIRATION_SECONDS)
            .build()
        setConfigSettingsAsync(configSettings)
        fetch(CONFIG_CACHE_EXPIRATION_SECONDS)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    fetchAndActivate()
                }
            }
    }

    override fun getPaymentInfo(): VipPaymentInfo =
        read<VipPaymentInfo>(ConfigParam.PAYMENT_INFO) ?: VipPaymentInfo()

    private inline fun <reified T> read(param: ConfigParam): T? = read(param, T::class.java)

    private fun <T> read(param: ConfigParam, returnType: Class<T>): T? {
        val value: Any? = when (returnType) {
            String::class.java -> config.getString(param.key)
            Boolean::class.java -> config.getBoolean(param.key)
            Long::class.java -> config.getLong(param.key)
            Int::class.java -> config.getLong(param.key).toInt()
            Double::class.java -> config.getDouble(param.key)
            Float::class.java -> config.getDouble(param.key).toFloat()
            else -> {
                val json = config.getString(param.key)
                json.takeIf { it.isNotBlank() }?.let { gson.jsonToObjectOrNull(json, returnType) }
            }
        }
        @Suppress("UNCHECKED_CAST")
        return (value as? T)
    }

    private enum class ConfigParam(val key: String) {
        PAYMENT_INFO("vipPaymentInfo"),
    }

    private companion object {
        /**
         * Config expiration interval 60 minutes.
         */
        private const val CONFIG_CACHE_EXPIRATION_SECONDS = 3600L
    }

}

interface IRemoteConfigApi {
    fun getPaymentInfo(): VipPaymentInfo
}