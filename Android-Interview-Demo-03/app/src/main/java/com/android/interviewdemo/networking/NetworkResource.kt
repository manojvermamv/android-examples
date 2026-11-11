package com.android.interviewdemo.networking

import com.android.interviewdemo.utils.Resource

class NetworkResource<T>(private val apiCall: suspend () -> Resource<T>) :
    BaseNetworkResource<T>() {

    override suspend fun apiCall(): Resource<T> {
        return apiCall.invoke()
    }

}

class NetworkResourceIndex<P, T>(private val apiCall: suspend (Int) -> Resource<T>) :
    BaseNetworkResource<T>() {

    private var pageIndex = 1
    private val allData = arrayListOf<P>() // To hold all data received

    override suspend fun apiCall(): Resource<T> {
        return apiCall.invoke(pageIndex)
    }

    fun updatePageIndex(newPageIndex: Int) {
        if (pageIndex == newPageIndex) return
        pageIndex = newPageIndex
        refresh()
    }

    fun getCurrentPage(): Int = pageIndex

    /**
     * Manage all data across pages
     * */
    fun resetData() {
        allData.clear()
        pageIndex = 1
        refresh()
    }

    fun getAllData(): ArrayList<P> {
        return allData
    }

    fun appendList(data: List<P>): ArrayList<P> {
        data.onEach {
            if (!allData.contains(it)) {
                allData.add(it)
            }
        }
        return allData
    }

}