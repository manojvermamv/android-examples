package com.anubhav.codereverse.uiapp.ui.common

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.transition.Fade
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.*
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.anubhav.codereverse.uiapp.ui.common.BaseActivity.Companion.PERMISSION_REQUEST_CODE
import com.anubhav.codereverse.uiapp.ui.common.BaseActivity.Companion.permissions
import com.anubhav.codereverse.uiapp.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

abstract class BaseActivity<VB : ViewDataBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    val preferences get() = MyApp.preference

    // Stores RxJava calls
    private val compositeDisposable = CompositeDisposable()
    val currentUid: String = FirebaseAuth.getInstance().currentUser?.uid ?: MyApp.userID

    //lateinit var baseViewModel: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //baseViewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)
        //baseViewModel.message.observe(this, EventObserver { showToast(it) })

        _binding = getViewBinding()
        //binding.lifecycleOwner = this.javaClass.newInstance()
        setContentView(binding.root)

        preferences.lastRefreshTime(System.currentTimeMillis())
        onCreateApp(savedInstanceState)
    }

    abstract fun getViewBinding(): VB

    abstract fun onCreateApp(savedInstanceState: Bundle?)

    fun viewModelFactory(otherUserID: String? = null) = ViewModelFactory(application as MyApp, otherUserID)

    inline fun <reified T : ViewModel> initViewModel(otherUserID: String? = null): T =
        ViewModelProvider(this, viewModelFactory(otherUserID)).get(T::class.java)

    fun <T> LiveData<T>.observe(function: (T) -> Unit) =
        this.observe(this@BaseActivity, Observer { it ->
            it?.let { function.invoke(it) }
        })


    override fun onStop() {
        super.onStop()
        // Clear all RxJava calls that have not yet completed
        compositeDisposable.clear()
    }

    /**
     * Creates an RxJava equivalent to post/postDelayed
     * The disposable is cleared on pause
     * So functions that act on the view can be safely
     * invoked without causing a crash
     */
    protected fun postSafely(callback: () -> Unit, delay: Long = 0, delayTimeUnit: TimeUnit = TimeUnit.SECONDS) {
        // Be careful of the RxJava call ordering
        // Delay must be placed before observeOn
        Completable.complete()
            .subscribeOn(Schedulers.newThread())
            .delay(delay, delayTimeUnit)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(callback)
            .subscribe()
            .addToDisposables()
    }

    private fun Disposable.addToDisposables() = compositeDisposable.add(this)

    fun signOut() = (application as MyApp).authManager.signOut {
        preferences.clear()
        goToLoginActivity()
    }

    fun goToLoginActivity() {
        val i = Intent(this, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        try {
            finishAffinity()
        } catch (e: Exception) {
            finish()
        }
    }

    companion object {
        const val TAG = "BaseActivity"
        const val PERMISSION_REQUEST_CODE = 1002
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }
}


/**
 * use for activity helper
 * */

fun BaseActivity<*>.excludeStatusAndNavBarFromTransition() {
    val fade = Fade()
    fade.excludeTarget(android.R.id.statusBarBackground, true)
    fade.excludeTarget(android.R.id.navigationBarBackground, true)
    window.enterTransition = fade
    window.exitTransition = fade
}

fun BaseActivity<*>.checkPermissions(): Boolean {
    for (permission in permissions) {
        if (ActivityCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
            return false
        }
    }
    return true
}