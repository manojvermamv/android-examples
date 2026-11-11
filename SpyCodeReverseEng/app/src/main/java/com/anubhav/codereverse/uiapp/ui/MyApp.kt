package com.anubhav.codereverse.uiapp.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.anubhav.commonutility.SoundPoolManager
import com.anubhav.codereverse.R
import com.anubhav.codereverse.uiapp.database.firebase.remote.FirebaseAuthManager
import com.anubhav.codereverse.uiapp.database.firebase.remote.RemoteConfigManager
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseAuthRepository
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseDatabaseRepository
import com.anubhav.codereverse.uiapp.database.preferences.SharedPreferenceManager
import com.anubhav.codereverse.uiapp.fcm.MyFirebaseMessagingService
import com.anubhav.codereverse.uiapp.firebase.common.database
import com.anubhav.codereverse.uiapp.utils.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson

class MyApp : Application() {

    val authManager by lazy { FirebaseAuthManager(GoogleSignIn.getClient(this, gso)) }
    val authRepository by lazy { FirebaseAuthRepository(this, authManager) }
    val databaseRepository by lazy { FirebaseDatabaseRepository() }

    val gso by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    companion object {
        lateinit var application: MyApp
            private set

        @JvmStatic val appContext: Context by lazy { application.applicationContext }

        @JvmStatic val remoteConfig by lazy { RemoteConfigManager(Gson()) }
        @JvmStatic val preference by lazy { SharedPreferenceManager.get(application.applicationContext) }
        @JvmStatic val soundPoolManager: SoundPoolManager by lazy { SoundPoolManager.getInstance(
            application.applicationContext, R.raw.incoming, R.raw.disconnect) }

        @JvmStatic val userID by lazy { preference.loggedUID() }
        @JvmStatic val country by lazy { Utils.getCurrentCountry(application.applicationContext) ?: "" }
        @JvmStatic val countryName by lazy { Utils.getCurrentCountryName(application.applicationContext) ?: ""}
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        //MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        FirebaseApp.initializeApp(applicationContext)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        database.keepSynced(true)

        registerActivityLifecycleCallbacks(ApplicationLifecycleManager())
        ProcessLifecycleOwner.get().lifecycle.addObserver(authManager)
        MyFirebaseMessagingService.createNotificationChannel(applicationContext)
    }

}