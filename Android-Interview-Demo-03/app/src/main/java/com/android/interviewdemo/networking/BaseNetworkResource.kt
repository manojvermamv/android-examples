package com.android.interviewdemo.networking

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.android.interviewdemo.utils.Resource

abstract class BaseNetworkResource<T> {

    /**
     * This is the MediatorLiveData that will observe changes in `dataSrc`
     * */
    private val mediatorData = MediatorLiveData<Resource<T>>()

    protected val _refreshTrigger: MutableLiveData<Unit> = MutableLiveData(Unit)

    protected abstract suspend fun apiCall(): Resource<T>

    private val dataSrc: LiveData<Resource<T>> by lazy {
        _refreshTrigger.switchMap {
            liveData {
                emit(Resource.Loading)
                emit(apiCall())
            }
        }
    }

    fun refresh() {
        _refreshTrigger.value = Unit
    }

    /**
     * Call this method once _refreshTrigger has a value to start observing.
     * Add the data source once _refreshTrigger has a valid value.
     */
    private fun startObservingData() {
        if (mediatorData.hasActiveObservers().not()) {
            mediatorData.addSource(dataSrc) { resource ->
                mediatorData.value = resource // Pass through the latest data changes
            }
        }
    }

    /**
     * Make sure first calling get() to start observing data
     * Get method that triggers a refresh and starts observing data
     */
    fun get(): LiveData<Resource<T>> {
        refresh() // Trigger refresh by updating _refreshTrigger
        startObservingData() // Add the data source after refresh
        return mediatorData // Return MediatorLiveData for observation
    }

    fun getOrObserve(
        lifecycleOwner: LifecycleOwner,
        onLoading: (Boolean) -> Unit,
        onSuccess: (T) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        get().observeLiveData(lifecycleOwner, onLoading, onSuccess, onError)
    }

    /**
     * Observe the MediatorLiveData Directly
     * */
    fun observe(
        lifecycleOwner: LifecycleOwner,
        onLoading: (Boolean) -> Unit,
        onSuccess: (T) -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        //startObservingData()
        mediatorData.observeLiveData(lifecycleOwner, onLoading, onSuccess, onError)
    }

    /**
     * Get the current value from MediatorLiveData, which holds Resource<T>
     * */
    fun getData(): T? {
        val resource = mediatorData.value
        return if (resource is Resource.Success) resource.data else null
    }

}

/*sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val exception: Exception) : Resource<Nothing>()
}*/

fun <T> LiveData<Resource<T>>.observeLiveData(
    lifecycleOwner: LifecycleOwner,
    onLoading: (Boolean) -> Unit,
    onSuccess: (T) -> Unit,
    onError: (String?) -> Unit = {}
) {
    observe(lifecycleOwner, Observer {
        when (it) {
            is Resource.Loading -> {
                onLoading(true)
            }

            is Resource.Success -> {
                onLoading(false)
                onSuccess(it.data)
            }

            is Resource.Error -> {
                onLoading(false)
                onError(it.exception.message)
            }

            else -> Unit
        }
    })
}