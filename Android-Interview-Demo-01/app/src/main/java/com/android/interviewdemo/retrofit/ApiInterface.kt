package com.android.interviewdemo.retrofit

import com.android.interviewdemo.model.User
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    @GET("users")
    fun getUsers(): Call<List<User>>

}