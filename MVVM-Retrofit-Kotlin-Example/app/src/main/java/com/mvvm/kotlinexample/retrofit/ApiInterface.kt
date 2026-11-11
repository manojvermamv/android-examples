package com.mvvm.kotlinexample.retrofit

import com.mvvm.kotlinexample.model.ServicesSetterGetter
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    @GET("services")
    fun getServices(): Call<ServicesSetterGetter>

}