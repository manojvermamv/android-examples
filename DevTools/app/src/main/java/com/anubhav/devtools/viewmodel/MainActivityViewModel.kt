package com.anubhav.devtools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.anubhav.devtools.utils.SingleEvent
import com.anubhav.devtools.model.repository.MainRepository

class MainActivityViewModel(
    context: Application,
    private val mainRepository: MainRepository,
) : AndroidViewModel(context) {

    private val _contactsRefreshTrigger = MutableLiveData<Unit>()
    private val _toolsRefreshTrigger = MutableLiveData<Unit>()

    /**
     * LiveData functionality
     * */
    val toolsLiveData = _toolsRefreshTrigger.switchMap {
        liveData { emit(mainRepository.fetchTools()) }
    }

    val contactsLiveData = _contactsRefreshTrigger.switchMap {
        liveData { emit(mainRepository.fetchContacts()) }
    }

    /**
     * Refresh functionality
     * */
    fun refreshContacts() {
        _contactsRefreshTrigger.value = Unit
    }

    fun refreshTools() {
        _toolsRefreshTrigger.value = Unit
    }

    /**
     * Fetch data from api request
     * */
    var getUsers = liveData {
        emit(mainRepository.getUsersApiCall())
    }

    /**
     * UI actions as event, user action is single one time event, Shouldn't be multiple time consumption
     */

    private val triggerProgressBarPrivate = MutableLiveData<SingleEvent<Boolean>>()
    val triggerProgressBar: LiveData<SingleEvent<Boolean>> get() = triggerProgressBarPrivate

    fun triggerProgressBar(show: Boolean) {
        triggerProgressBarPrivate.value = SingleEvent(show)
    }

}