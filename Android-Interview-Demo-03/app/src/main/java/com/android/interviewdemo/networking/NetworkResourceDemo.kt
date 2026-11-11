package com.android.interviewdemo.networking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.android.interviewdemo.utils.Resource

/*
val networkResourceDemo = NetworkResourceDemo<ApiData, UserData>(
        apiCall = { apiParams ->
            // Make your API call here
            apiRepository.getUserDataApiCall()
        },
        initialParams = ApiData(pageIndex = "2", filter = "categoryA")
    )
* */

interface ApiParams

data class ApiData(val pageIndex: String, val filter: String? = null) : ApiParams

class NetworkResourceDemo<P : ApiParams, T>(
    private val apiCall: suspend (params: P) -> Resource<T>,
    initialParams: P
) {

    private var params: P = initialParams // Initialize with initialParams

    private val _refreshTrigger: MutableLiveData<out P> = MutableLiveData<P>()

    val data: LiveData<Resource<T>> by lazy {
        _refreshTrigger.switchMap { params ->
            liveData {
                emit(Resource.Loading)
                emit(apiCall(params))
            }
        }
    }

    fun refresh() {
        _refreshTrigger.value = params
    }

    fun updateParams(newParams: P) {
        params = newParams
        refresh()
    }
}