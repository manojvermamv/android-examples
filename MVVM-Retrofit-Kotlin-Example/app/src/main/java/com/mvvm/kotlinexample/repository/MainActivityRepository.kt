package com.mvvm.kotlinexample.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mvvm.kotlinexample.model.ServicesSetterGetter
import com.mvvm.kotlinexample.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object MainActivityRepository {

    val serviceSetterGetter = MutableLiveData<ServicesSetterGetter>()

    fun getServicesApiCall(): MutableLiveData<ServicesSetterGetter> {
        RetrofitClient.apiInterface.getServices().enqueueApiCall({ data ->
            serviceSetterGetter.value = data
        }, { error ->
            // Handle error gracefully
            Log.e("DEBUG", "Error fetching services: ${error.message}")
            serviceSetterGetter.value = ServicesSetterGetter("Error fetching services: ${error.message}")
        })
        return serviceSetterGetter
    }

}

fun <T> Call<T>.enqueueApiCall(
    onSuccess: (T) -> Unit,
    onFailure: (Throwable) -> Unit = { Log.v("RetrofitClient onFailure: ", it.message.toString()) }
) {
    enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>, t: Throwable) {
            onFailure(t)
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            Log.v("RetrofitClient onResponse: ", response.body().toString())
            if (response.isSuccessful) {
                onSuccess(response.body()!!)
            } else {
                onFailure(Throwable("API call failed with status code ${response.code()}"))
            }
        }
    })
}