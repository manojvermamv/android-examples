package com.anubhav.codereverse.uiapp.ui.register

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.anubhav.codereverse.R
import com.anubhav.codereverse.uiapp.common.Event
import com.anubhav.codereverse.uiapp.common.SingleLiveEvent
import com.anubhav.codereverse.uiapp.common.Result
import com.anubhav.codereverse.uiapp.database.firebase.entity.UserInfo
import com.anubhav.codereverse.uiapp.database.firebase.entity.mkUser
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseAuthRepository
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseDatabaseRepository
import com.anubhav.codereverse.uiapp.ui.common.BaseViewModel
import com.anubhav.commonutility.HelperMethod

class RegisterViewModel(
    authRepo: FirebaseAuthRepository,
    private val dbRepository: FirebaseDatabaseRepository,
    private val app: Application
) : BaseViewModel(authRepo) {
    private var email: String? = null
    private var password: String? = null

    private val _goToRegisterInfoScreen = SingleLiveEvent<Unit>()
    val goToRegisterInfoScreen = _goToRegisterInfoScreen

    private val _isLoggedInEvent = MutableLiveData<Event<UserInfo>>()
    val isLoggedInEvent: LiveData<Event<UserInfo>> = _isLoggedInEvent

    fun onEmailEntered(email: String, password: String) {
        onResult(null, Result.Loading)
        if (HelperMethod.validateEmail(email) && HelperMethod.validatePassword(password)) {
            this.email = email
            this.password = password
            authRepository.isUserExistsForEmail(email) { result ->
                onResult(null, result)
                if (result is Result.Success) {
                    setMessage(app.getString(R.string.this_email_already_exists))
                } else {
                    _goToRegisterInfoScreen.call()
                }
            }
        } else {
            onResult(null, Result.Error(app.getString(R.string.please_enter_valid_email_and_password)))
        }
    }

    fun onRegister() {
        onResult(null, Result.Loading)
        if (email != null && password != null) {
            authRepository.createUser(mkUser(email = email!!, password = password)) { result ->
                if (result is Result.Success) {
                    val newUser = result.data!!
                    _isLoggedInEvent.value = Event(newUser)
                }
                onResult(null, result)
            }
        } else {
            onResult(null, Result.Error(app.getString(R.string.please_enter_email)))
        }
    }

}