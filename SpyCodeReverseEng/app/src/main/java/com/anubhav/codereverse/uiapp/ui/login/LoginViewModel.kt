package com.anubhav.codereverse.uiapp.ui.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.anubhav.codereverse.R
import com.anubhav.codereverse.uiapp.common.Result
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseAuthRepository
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseDatabaseRepository
import com.anubhav.codereverse.uiapp.ui.common.BaseViewModel
import com.anubhav.commonutility.HelperMethod
import com.google.firebase.auth.FirebaseUser

class LoginViewModel(
    authRepo: FirebaseAuthRepository,
    private val dbRepository: FirebaseDatabaseRepository,
    private val app: Application
) : BaseViewModel(authRepo) {

    private val _isLoggedInEvent = MutableLiveData<FirebaseUser>()
    val isLoggedInEvent: LiveData<FirebaseUser> = _isLoggedInEvent

    fun onLoginClick(email: String, password: String) {
        onResult(null, Result.Loading)
        if (HelperMethod.validateEmail(email) && HelperMethod.validatePassword(password)) {
            authRepository.loginUser(email, password) { result: Result<FirebaseUser> ->
                onResult(_isLoggedInEvent, result)
            }
        } else {
            onResult(null, Result.Error(app.getString(R.string.please_enter_valid_email_and_password)))
        }
    }

}