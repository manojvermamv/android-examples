package com.android.interviewdemo.retrofit

import com.android.interviewdemo.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val MAIN_SERVER = "https://jsonplaceholder.typicode.com/"

    private val retrofitClient: Retrofit.Builder by lazy {
        val levelType: HttpLoggingInterceptor.Level =
            if (BuildConfig.BUILD_TYPE.contentEquals("debug"))
                HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

        val logging = HttpLoggingInterceptor()
        logging.setLevel(levelType)

        val okHttpClient = OkHttpClient.Builder()
        okHttpClient.addInterceptor(logging)

        Retrofit.Builder()
            .baseUrl(MAIN_SERVER)
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
    }

    val apiInterface: ApiInterface by lazy {
        retrofitClient.build().create(ApiInterface::class.java)
    }

}