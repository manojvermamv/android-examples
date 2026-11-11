package com.android.interviewdemo.networking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.android.interviewdemo.utils.Resource

/**
 * ViewModel Use Case: Initialize NetworkResources with initial parameters
 * ```kotlin
 * val featuredTournamentsResource = NetworkResourceArray<String, YourDataType>({ params ->
 *          apiRepository.getFeaturedTournamentsApiCall(*params)
 *     },
 *     arrayOf("1", "2") // Provide initial parameters
 * )
 * ```
 * Use updateParams to trigger refresh with new parameters:
 * ```kotlin
 * fun refreshFeaturedTournaments() {
 *     featuredTournamentsResource.updateParams("3", "4", "5")
 * }
 * ```
 */

class NetworkResourceArray<P : Any, T>(
    private val apiCall: suspend (Array<out P>) -> Resource<T>,
    initialParams: Array<P> // Pass initial parameters to the constructor
) : BaseNetworkResourceArray<P, T>() {

    override val _refreshTrigger = MutableLiveData<Array<out P>>()

    private var params: Array<out P> = initialParams

    override suspend fun apiCall(vararg params: P): Resource<T> {
        return apiCall.invoke(params)
    }

    // Trigger the refresh by updating _refreshTrigger with current params
    override fun refresh() {
        _refreshTrigger.value = params
    }

    // Update the params and refresh as List<P>
    fun updateParams(vararg newParams: P) {
        params = newParams
        refresh()
    }
}

abstract class BaseNetworkResourceArray<P : Any, T> {

    // Use MutableLiveData with Array<out P> to handle multiple parameters
    protected abstract val _refreshTrigger: MutableLiveData<Array<out P>>

    // Use P as the parameter type in the switchMap
    val data: LiveData<Resource<T>> by lazy {
        _refreshTrigger.switchMap { params ->
            liveData {
                emit(Resource.Loading)
                emit(apiCall(*params)) // Spread operator (*) for vararg
            }
        }
    }

    // Abstract apiCall to accept vararg to accept multiple parameters of type P
    protected abstract suspend fun apiCall(vararg params: P): Resource<T>

    // Abstract refresh function that will trigger refresh in subclasses
    abstract fun refresh()
}