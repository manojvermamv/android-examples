package com.android.interviewdemo.networking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.android.interviewdemo.utils.Resource

/**
 * ViewModel Use Case: Initialize NetworkResources with initial parameters
 * ```kotlin
 * val featuredTournamentsResource = NetworkResourceList<String, YourDataType>({ params ->
 *          apiRepository.getFeaturedTournamentsApiCall(params)
 *    },
 *    listOf("1", "2") // Provide initial parameters as a List
 * )
 * ```
 * Use updateParams to trigger refresh with new parameters:
 * ```kotlin
 * fun refreshFeaturedTournaments() {
 *     featuredTournamentsResource.updateParams("3", "4", "5")
 * }
 * ```
 */

class NetworkResourceList<P : Any, T>(
    private val apiCall: suspend (List<P>) -> Resource<T>,
    initialParams: List<P> = emptyList() // Use List<P> for initial parameters
) : BaseNetworkResourceList<P, T>() {

    override val _refreshTrigger = MutableLiveData<List<P>>()

    private var params: List<P> = initialParams

    override suspend fun apiCall(params: List<P>): Resource<T> {
        return apiCall.invoke(params)
    }

    // Trigger the refresh by updating _refreshTrigger with current params
    override fun refresh() {
        _refreshTrigger.value = params
    }

    // Update the params and refresh as List<P>
    fun updateParams(vararg newParams: P) {
        params = newParams.toList()
        refresh()
    }
}

abstract class BaseNetworkResourceList<P : Any, T> {

    // Use MutableLiveData with List<P> to handle multiple parameters
    protected abstract val _refreshTrigger: MutableLiveData<List<P>>

    // Use P as the parameter type in the switchMap
    val data: LiveData<Resource<T>> by lazy {
        _refreshTrigger.switchMap { params ->
            liveData {
                emit(Resource.Loading)
                emit(apiCall(params)) // Pass the list directly
            }
        }
    }

    // Abstract apiCall to accept a List<P> instead of vararg to accept multiple parameters of type P
    protected abstract suspend fun apiCall(params: List<P>): Resource<T>

    // Abstract refresh function that will trigger refresh in subclasses
    abstract fun refresh()
}