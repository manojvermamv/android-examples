package com.anubhav.codereverse.uiapp.utils

import android.util.Log
import com.anubhav.codereverse.BuildConfig

/*
* Wrapper for android.util.Log to disable logging
*/
object Log {

    private val LOGGING = BuildConfig.DEBUG

    private fun contextString(context: Any): String {
        if (context is String) {
            return context
        } else {
            return context.javaClass.simpleName
        }
    }

    @JvmStatic
    fun d(context: Any, message: String) {
        if (LOGGING) {
            val tag = contextString(context)
            Log.d(tag, message)
        }
    }

    @JvmStatic
    fun i(context: Any, message: String) {
        if (LOGGING) {
            val tag = contextString(context)
            Log.i(tag, message)
        }
    }

    @JvmStatic
    fun w(context: Any, message: String) {
        if (LOGGING) {
            val tag = contextString(context)
            Log.w(tag, message)
        }
    }

    @JvmStatic
    fun e(context: Any, message: String) {
        if (LOGGING) {
            val tag = contextString(context)
            Log.e(tag, message)
        }
    }

    @JvmStatic
    fun getErrors(context: Any, error: String) {
        if (LOGGING) {
            val tag = contextString(context)
            Log.e(tag, error)
        }
    }

    @JvmStatic
    fun getErrors(context: Any, error: Exception) {
        if (LOGGING) {
            val tag = contextString(context)
            Log.e(tag, Log.getStackTraceString(error))
        }
    }

}