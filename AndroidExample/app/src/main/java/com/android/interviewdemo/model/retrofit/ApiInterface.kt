package com.android.interviewdemo.model.retrofit

import com.android.interviewdemo.model.models.User
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    @GET("users")
    fun getUsers(): Call<List<User>>

}