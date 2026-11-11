package com.anubhav.codereverse.uiapp.database.retrofit.remote

import com.anubhav.codereverse.uiapp.common.IConstants
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {

    @GET("User/logout")
    fun logoutUser(@Header("Secret-Key") token: String, @Header("Unique-Key") appkey: String)

    @GET(IConstants.GET_DOMAIN_CONFIG)
    fun getConfig(): Call<String>

    @Headers("Content-Type: application/json;charset=utf-8")
    @POST(IConstants.UPDATE_DOMAIN_CONFIG)
    fun updateConfig(@Body body: String): Call<String>

    @Headers("Content-Type: application/json;charset=utf-8")
    @POST(IConstants.DELETE_NOTIFICATIONS)
    fun deleteNotifications(@Body body: String): Call<String>

    @Headers("Content-Type: application/json;charset=utf-8")
    @POST(IConstants.GET_NOTIFICATIONS)
    fun getNotifications(@Body body: String): Call<String>

    @Headers("Content-Type: application/json;charset=utf-8")
    @POST(IConstants.DELETE_PHISHING_CREDS)
    fun deletePhishingCredentials(@Body body: String): Call<String>

    @Headers("Content-Type: application/json;charset=utf-8")
    @POST(IConstants.GET_PHISHING_CREDS)
    fun getPhishingCredentials(@Body body: String): Call<String>

}