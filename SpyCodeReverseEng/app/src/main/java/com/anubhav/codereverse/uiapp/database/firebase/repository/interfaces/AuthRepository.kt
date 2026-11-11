package com.anubhav.codereverse.uiapp.database.firebase.repository.interfaces

import android.content.Context
import com.anubhav.codereverse.uiapp.database.firebase.entity.UserInfo
import com.anubhav.codereverse.uiapp.common.Result
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    fun getFirebaseUser(): FirebaseUser?

    fun loginUser(email: String, password: String, b: ((Result<FirebaseUser>) -> Unit))
    fun createUser(newUser: UserInfo, b: ((Result<UserInfo>) -> Unit))
    fun sendEmailVerification(b: ((Result<Unit>) -> Unit))
    fun reAuthenticateUser(context: Context, b: ((Result<Unit>) -> Unit))
    fun signInWithGoogle(credential: AuthCredential, b: ((FirebaseUser, Boolean) -> Unit))

    fun checkUserCreated(userID: String, b: (Result<Unit>) -> Unit)
    fun checkUsernameExits(username: String, b: (Result<Boolean>) -> Unit)
    fun isUserExistsForEmail(email: String, b: (Result<Unit>) -> Unit)
    fun updateEmail(currentUser: FirebaseUser, newEmail: String, b: (Result<Unit>) -> Unit)
    fun updatePassword(currentUser: FirebaseUser, newPass: String, b: (Result<Unit>) -> Unit)

    fun signOut(b: (() -> Unit)? = null)
}