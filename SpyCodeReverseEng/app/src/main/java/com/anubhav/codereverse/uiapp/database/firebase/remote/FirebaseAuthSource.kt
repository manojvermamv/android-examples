package com.anubhav.codereverse.uiapp.database.firebase.remote

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.anubhav.codereverse.uiapp.firebase.common.auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthStateObserver {

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var instance: FirebaseAuth? = null

    fun start(valueEventListener: FirebaseAuth.AuthStateListener, instance: FirebaseAuth) {
        this.authListener = valueEventListener
        this.instance = instance
        this.instance?.addAuthStateListener(authListener!!)
    }

    fun clear() {
        authListener?.let { instance?.removeAuthStateListener(it) }
    }
}

class FirebaseAuthManager(private val gsoClient: GoogleSignInClient? = null) : DefaultLifecycleObserver {

    private var listener: FirebaseAuth.AuthStateListener
    private val fbAuthStateObserver = FirebaseAuthStateObserver()
    private val fbRefConnectedObserver = FirebaseReferenceConnectedObserver()

    init {
        listener = FirebaseAuth.AuthStateListener {
            val currentUser = it.currentUser
            if (currentUser != null) {
                fbRefConnectedObserver.start(currentUser.uid)
            } else {
                fbRefConnectedObserver.clear()
                //signOut()
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // App visible/foreground
        if (owner.lifecycle.currentState == Lifecycle.State.RESUMED) {
            fbAuthStateObserver.start(listener, auth)
            fbRefConnectedObserver.setOnlineStatus(true)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        // App in background
        if (owner.lifecycle.currentState == Lifecycle.State.STARTED) {
            fbAuthStateObserver.clear()
            fbRefConnectedObserver.setOnlineStatus(false)
        }
    }

    /**
     * extra
     * */
    fun getFirebaseUser(): FirebaseUser? = auth.currentUser

    fun signIn(email: String, password: String): Task<AuthResult> =
        auth.signInWithEmailAndPassword(email, password)

    fun signIn(credential: AuthCredential): Task<AuthResult> =
        auth.signInWithCredential(credential)

    fun createUser(email: String, password: String): Task<AuthResult> =
        auth.createUserWithEmailAndPassword(email, password)

    fun reAuthenticateUser(credential: AuthCredential): Task<Void>? =
        auth.currentUser?.reauthenticate(credential)

    fun signOut(b: (() -> Unit)? = null) {
        fbAuthStateObserver.clear()
        fbRefConnectedObserver.clear() // must call before FirebaseAuth.signOut()
        auth.signOut()
        gsoClient?.signOut()
        b?.invoke()
    }

}

