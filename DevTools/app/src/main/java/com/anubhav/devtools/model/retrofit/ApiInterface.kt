package com.anubhav.devtools.model.retrofit

import com.anubhav.devtools.model.models.User
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    @GET("users")
    fun getUsers(): Call<List<User>>

}