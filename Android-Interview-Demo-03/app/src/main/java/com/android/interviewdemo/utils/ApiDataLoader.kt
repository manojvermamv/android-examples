package com.android.interviewdemo.utils

class ApiDataLoader<T>(private val apiCall: suspend () -> Resource<T>) :
    BaseApiDataLoader<T>() {

    override suspend fun apiCall(): Resource<T> {
        return apiCall.invoke()
    }
}

class ApiDataLoaderPaged<P, T>(private val apiCall: suspend (Int) -> Resource<T>) :
    BaseApiDataLoader<T>() {

    private var pageIndex = 1
    private val allData = arrayListOf<P>() // To hold all data received

    override suspend fun apiCall(): Resource<T> {
        return apiCall.invoke(pageIndex)
    }

    fun updatePageIndex(newPageIndex: Int) {
        if (pageIndex != newPageIndex) {
            pageIndex = newPageIndex
            refresh()
        }
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
        allData.addAll(data.distinct())
        return allData
    }

}