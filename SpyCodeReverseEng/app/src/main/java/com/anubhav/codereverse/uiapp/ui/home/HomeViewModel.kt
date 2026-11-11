package com.anubhav.codereverse.uiapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.anubhav.codereverse.uiapp.common.Result
import com.anubhav.codereverse.uiapp.data.model.DefaultConfig
import com.anubhav.codereverse.uiapp.database.firebase.entity.UserNotification
import com.anubhav.codereverse.uiapp.database.firebase.remote.FirebaseReferenceValueObserver
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseAuthRepository
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseDatabaseRepository
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.anubhav.codereverse.uiapp.ui.common.BaseViewModel

class HomeViewModel(
    authRepo: FirebaseAuthRepository,
    private val dbRepository: FirebaseDatabaseRepository
) : BaseViewModel(authRepo) {

    init {
        loadConfig()
    }

    /**
     * Notifications Observer
     * */
    private val referenceValueObserver = FirebaseReferenceValueObserver()

    private val _userNotificationsList = MutableLiveData<MutableList<UserNotification>>()
    var userNotificationsList: LiveData<MutableList<UserNotification>> = _userNotificationsList

    fun startObservingNotifications() {
        dbRepository.loadAndObserveUserNotifications(currentUid, referenceValueObserver) { result ->
            if (result is Result.Success) {
                _userNotificationsList.value = result.data!!
            }
        }
    }

    fun stopObservingNotifications() {
        referenceValueObserver.clear()
    }

    private fun loadConfig() {
        dbRepository.loadAndObserveConfig(FirebaseReferenceValueObserver()) { result: Result<DefaultConfig> ->
            onResult(null, result)
            if (result is Result.Success && result.data != null) {
                MyApp.preference.defaultConfig(result.data)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        referenceValueObserver.clear()
    }

}