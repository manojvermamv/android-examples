package com.android.interviewdemo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.interviewdemo.model.models.User
import com.android.interviewdemo.utils.SingleEvent
import com.android.interviewdemo.viewmodel.repository.MainActivityRepository

class NavActivityViewModel : ViewModel() {

    /**
     * UI actions as event, user action is single one time event, Shouldn't be multiple time consumption
     */
    private val openHomeScreenPrivate = MutableLiveData<SingleEvent<String>>()
    val openHomeScreen: LiveData<SingleEvent<String>> get() = openHomeScreenPrivate

    fun openHomeScreen(id: String) {
        openHomeScreenPrivate.value = SingleEvent(id)
    }

    private val openDetailsScreenPrivate = MutableLiveData<SingleEvent<String>>()
    val openDetailsScreen: LiveData<SingleEvent<String>> get() = openDetailsScreenPrivate

    fun openDetailsScreen(id: String) {
        openDetailsScreenPrivate.value = SingleEvent(id)
    }

    private val backToHomeScreenPrivate = MutableLiveData<Unit>()
    val backToHomeScreen: LiveData<Unit> get() = backToHomeScreenPrivate

    fun backToHomeScreen() {
        backToHomeScreenPrivate.value = Unit
    }

}