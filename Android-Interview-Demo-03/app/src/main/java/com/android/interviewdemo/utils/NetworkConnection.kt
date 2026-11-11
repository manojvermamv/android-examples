package com.android.interviewdemo.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import androidx.lifecycle.LiveData


class NetworkConnection(private val context: Context) : LiveData<Boolean>() {

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkConnectionCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            postValue(true)
        }

        override fun onLost(network: Network) {
            postValue(false)
        }
    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateNetworkConnectionLegacy()
        }
    }

    override fun onActive() {
        super.onActive()
        updateNetworkConnection()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // For API 24 and above
            connectivityManager.registerDefaultNetworkCallback(networkConnectionCallback)
        } else {
            // For older devices, use BroadcastReceiver
            @Suppress("DEPRECATION")
            context.registerReceiver(
                networkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    override fun onInactive() {
        super.onInactive()
        // Unregister callbacks
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.unregisterNetworkCallback(networkConnectionCallback)
        } else {
            context.unregisterReceiver(networkReceiver)
        }
    }

    /**
     * Update network status for API level 23 and above using NetworkCapabilities.
     */
    private fun updateNetworkConnection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            postValue(networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
        } else {
            updateNetworkConnectionLegacy() // Fallback for older API levels
        }
    }

    /**
     * Update network status for devices below API level 23 using the deprecated NetworkInfo.
     */
    @Suppress("DEPRECATION")
    private fun updateNetworkConnectionLegacy() {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        postValue(activeNetwork?.isConnected == true)
    }

}