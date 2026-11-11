package com.anubhav.codereverse.uiapp.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.anubhav.codereverse.uiapp.common.Result
import com.anubhav.codereverse.uiapp.database.firebase.entity.UserInfo
import com.anubhav.codereverse.uiapp.database.firebase.entity.mkUser
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseAuthRepository
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseDatabaseRepository
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.anubhav.codereverse.uiapp.ui.common.BaseViewModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(
    authRepo: FirebaseAuthRepository,
    private val dbRepository: FirebaseDatabaseRepository,
    private val app: MyApp
) : BaseViewModel(authRepo) {

    private val _signInWithGoogleEvent = MutableLiveData<FirebaseUser>()
    val signInWithGoogleEvent: LiveData<FirebaseUser> = _signInWithGoogleEvent

    fun signInWithGoogle(credential: AuthCredential) {
        onResult(null, Result.Loading)
        authRepository.signInWithGoogle(credential) { firebaseUser, isNewUsr ->
            authRepository.checkUserCreated(firebaseUser.uid) { result1 ->
                when (result1) {
                    is Result.Success -> {
                        onResult(_signInWithGoogleEvent, Result.Success(firebaseUser))
                    }
                    is Result.Error -> {
                        // save new User to database
                        val newUser = mkUser(firebaseUser).apply { isNewUser = isNewUsr }
                        dbRepository.updateNewUser(newUser)
                        authRepository.updatePassword(firebaseUser, newUser.password) { result2 ->
                            if (result2 is Result.Success) {
                                onResult(_signInWithGoogleEvent, Result.Success(firebaseUser))
                            } else {
                                onResult(null, result2)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun sendEmailVerification(newUser: UserInfo, b: (() -> Unit)) {
        onResult(null, Result.Loading)
        authRepository.sendEmailVerification { result ->
            onResult(null, result)
            if (result is Result.Success) {
                // update UserData in database after email verification
                dbRepository.updateNewUser(newUser) { b.invoke() }
            }
        }
    }

}