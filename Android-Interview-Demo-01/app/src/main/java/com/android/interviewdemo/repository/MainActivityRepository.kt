package com.android.interviewdemo.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.interviewdemo.model.User
import com.android.interviewdemo.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object MainActivityRepository {

    private val usersLiveData = MutableLiveData<List<User>>()

    fun getUsersApiCall(): MutableLiveData<List<User>> {
        RetrofitClient.apiInterface.getUsers().enqueueApiCall({ data ->
            usersLiveData.value = data
        }, { error ->
            // Handle error gracefully
            Log.e("DEBUG", "Error fetching services: ${error.message}")
        })
        return usersLiveData
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