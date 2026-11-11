package com.anubhav.codereverse.uiapp.database.firebase.repository

import android.content.Context
import com.anubhav.codereverse.uiapp.firebase.common.addOnFailure
import com.anubhav.codereverse.uiapp.common.Result
import com.anubhav.codereverse.uiapp.database.firebase.entity.UserInfo
import com.anubhav.codereverse.uiapp.database.firebase.remote.FirebaseAuthManager
import com.anubhav.codereverse.uiapp.database.firebase.repository.interfaces.AuthRepository
import com.anubhav.codereverse.uiapp.firebase.common.auth
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class FirebaseAuthRepository(private val myApp: MyApp, private val authManager: FirebaseAuthManager) : AuthRepository {

    private val dbRepository = FirebaseDatabaseRepository()

    override fun getFirebaseUser(): FirebaseUser? {
        return authManager.getFirebaseUser()
    }

    override fun loginUser(email: String, password: String, b: (Result<FirebaseUser>) -> Unit) {
        b.invoke(Result.Loading)
        authManager.signIn(email, password).addOnSuccessListener {
            val firebaseUser = it.user!!
            if (firebaseUser.isEmailVerified) {
                b.invoke(Result.Success(firebaseUser))
            } else {
                b.invoke(Result.Error("Verification email sent. Please verify first to login"))
            }
        }.addOnFailure(b)
    }

    override fun createUser(newUser: UserInfo, b: ((Result<UserInfo>) -> Unit)) {
        b.invoke(Result.Loading)
        authManager.createUser(newUser.email, newUser.password).addOnSuccessListener {
            newUser.uid = it.user?.uid ?: ""
            newUser.isNewUser = it.additionalUserInfo?.isNewUser ?: false
            b.invoke(Result.Success(newUser))
        }.addOnFailure(b)
    }

    override fun sendEmailVerification(b: (Result<Unit>) -> Unit) {
        b.invoke(Result.Loading)
        val firebaseUser = authManager.getFirebaseUser()
        if (firebaseUser != null) {
            if (!firebaseUser.isEmailVerified) {
                firebaseUser.sendEmailVerification().addOnSuccessListener {
                    b.invoke(Result.Success(Unit, "Verification email sent to " + firebaseUser.email + ".\nYou can login with your email and password"))
                }.addOnFailure(b)
            } else {
                b.invoke(Result.Error("Email is already verified"))
            }
        } else {
            b.invoke(Result.Error("Failed to send verification email."))
        }
    }

    override fun reAuthenticateUser(context: Context, b: ((Result<Unit>) -> Unit)) {
        b.invoke(Result.Loading)
        val signInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (signInAccount != null) {
            val credential: AuthCredential = GoogleAuthProvider.getCredential(signInAccount.idToken, null)
            authManager.reAuthenticateUser(credential)?.addOnSuccessListener {
                b.invoke(Result.Success(Unit))
            }?.addOnFailure(b) ?: b.invoke(Result.Error("reAuthenticateUser task is null"))
        } else b.invoke(Result.Error("GoogleSignIn account is null"))
    }

    override fun signInWithGoogle(credential: AuthCredential, b: (FirebaseUser, Boolean) -> Unit) {
        authManager.signIn(credential).addOnSuccessListener { authResult ->
            val firebaseUser = authResult?.user
            if (firebaseUser != null) {
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                b.invoke(firebaseUser, isNewUser)
            }
        }.addOnFailure()
    }

    /**
     * Check region
     * */

    override fun checkUserCreated(userID: String, b: (Result<Unit>) -> Unit) {
        dbRepository.checkUserCreated(userID, b)
    }

    override fun checkUsernameExits(username: String, b: (Result<Boolean>) -> Unit) {
        dbRepository.checkUsernameExits(username, b)
    }

    override fun isUserExistsForEmail(email: String, b: (Result<Unit>) -> Unit) {
        auth.fetchSignInMethodsForEmail(email).addOnSuccessListener {
            val signInMethods = it?.signInMethods ?: emptyList<String>()
            if (signInMethods.isNotEmpty()) {
                b.invoke(Result.Success(msg = "This email already used"))
            } else {
                b.invoke(Result.Error("This email not exists"))
            }
        }.addOnFailure(b)
    }

    override fun updateEmail(currentUser: FirebaseUser, newEmail: String, b: (Result<Unit>) -> Unit) {
        b.invoke(Result.Loading)
        currentUser.updateEmail(newEmail).addOnSuccessListener {
            dbRepository.updateUserEmail(currentUser.uid, newEmail).addOnSuccessListener {
                b.invoke(Result.Success(msg = "Email updated successfully"))
            }.addOnFailure(b)
        }.addOnFailure(b)
    }


    override fun updatePassword(currentUser: FirebaseUser, newPass: String, b: (Result<Unit>) -> Unit) {
        b.invoke(Result.Loading)
        currentUser.updatePassword(newPass).addOnSuccessListener {
            dbRepository.updateUserPassword(currentUser.uid, newPass).addOnSuccessListener {
                b.invoke(Result.Success(msg = "Password updated successfully"))
            }.addOnFailure(b)
        }.addOnFailure(b)
    }

    override fun signOut(b: (() -> Unit)?) {
        authManager.signOut(b)
    }

}