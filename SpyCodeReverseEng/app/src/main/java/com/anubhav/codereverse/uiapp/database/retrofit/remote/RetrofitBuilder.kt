package com.anubhav.codereverse.uiapp.database.retrofit.remote

import android.content.Context
import com.anubhav.codereverse.uiapp.database.preferences.domainBaseUrl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitBuilder {

    companion object {

        @JvmStatic
        fun create(context: Context): RetrofitService {
            val gson = GsonBuilder()
                .setLenient()
                .serializeNulls()
                .create()
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
            return Retrofit.Builder()
                .baseUrl(context.domainBaseUrl())
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(RetrofitService::class.java)
        }
    }

}

fun Call<String>.addCallback(callback: (Boolean, String) -> Unit) {
    enqueue(object : Callback<String> {
        override fun onResponse(call: Call<String>, response: Response<String>) {
            val body = response.body()?: ""
            callback(response.isSuccessful, body)
        }

        override fun onFailure(call: Call<String>, t: Throwable) {
            callback(false, t.message ?: "Request failure")
        }
    })
}

fun Response<String>.parseResponse(): JSONObject {
    return if (isSuccessful) {
        try {
            JSONObject(body())
        } catch (e: Exception) {
            e.printStackTrace()
            JSONObject().put("status", false).put("message", e.message ?: "An error occurred")
        }
    } else {
        JSONObject().put("status", false).put("message", "Error: ${errorBody().toString()}")
    }
}

fun String.parseResponse(): JSONObject {
    return try {
        JSONObject(this)
    } catch (e: Exception) {
        e.printStackTrace()
        JSONObject().put("status", false).put("message", e.message ?: "An error occurred")
    }
}