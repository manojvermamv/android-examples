package com.anubhav.codereverse.uiapp.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anubhav.codereverse.uiapp.common.Event
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseAuthRepository
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.anubhav.codereverse.uiapp.common.Result
import com.google.firebase.auth.FirebaseUser

abstract class BaseViewModel(protected val authRepository: FirebaseAuthRepository) : ViewModel() {
    private val _message = MutableLiveData<Event<String>>()
    var message: LiveData<Event<String>> = _message

    private val _dataLoading = MutableLiveData<Event<Boolean>>()
    val dataLoading: LiveData<Event<Boolean>> = _dataLoading

    protected fun <T> onResult(mutableLiveData: MutableLiveData<T>? = null, result: Result<T>) {
        when (result) {
            is Result.Loading -> _dataLoading.value = Event(true)

            is Result.Error -> {
                _dataLoading.value = Event(false)
                result.msg?.let { _message.value = Event(it) }
            }

            is Result.Success -> {
                _dataLoading.value = Event(false)
                result.data?.let { mutableLiveData?.value = it }
                result.msg?.let { _message.value = Event(it) }
            }
        }
    }

    fun setMessage(message: String?) {
        message?.let { _message.value = Event(it) }
    }

    /**
     *  default implementation for authStateObserver
     * */
    var firebaseUser: FirebaseUser? = authRepository.getFirebaseUser()
    var currentUid: String = firebaseUser?.uid ?: MyApp.userID

}