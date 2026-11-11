package com.anubhav.codereverse.uiapp.ui.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.anubhav.customprogresshud.CustomProgressHUD
import com.anubhav.codereverse.R
import com.anubhav.codereverse.databinding.ActivityRegisterBinding
import com.anubhav.codereverse.uiapp.common.EventObserver
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.anubhav.codereverse.uiapp.ui.common.toogleTouchable
import com.anubhav.codereverse.uiapp.ui.common.BaseActivity
import com.anubhav.codereverse.uiapp.ui.common.getCustomProgress
import com.anubhav.codereverse.uiapp.ui.common.showToast
import com.anubhav.codereverse.uiapp.ui.common.setViewHeight
import com.anubhav.codereverse.uiapp.ui.home.HomeActivity
import com.anubhav.codereverse.uiapp.utils.Utils
import com.anubhav.commonutility.GlobalData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.GoogleAuthProvider
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class RegisterActivity : BaseActivity<ActivityRegisterBinding>(),
    KeyboardVisibilityEventListener, EmailFragment.Listener {

    private lateinit var progress: CustomProgressHUD
    private val mViewModel: RegisterViewModel by viewModels { viewModelFactory() }
    private val authViewModel: AuthViewModel by viewModels { viewModelFactory() }

    override fun getViewBinding() = ActivityRegisterBinding.inflate(layoutInflater)

    override fun onCreateApp(savedInstanceState: Bundle?) {
        GlobalData.setStatusBarFullScreen(this)
        setViewHeight(binding.topView)
        KeyboardVisibilityEvent.setEventListener(this, this)

        // show progress bar
        progress = getCustomProgress()
        initSignInWithGoogle(binding.loginWithGoogle)
        binding.includeActionbar.imgBack.setOnClickListener { onBackPressed() }
        binding.loginAccountText.setOnClickListener { goToLoginActivity() }

        mViewModel.dataLoading.observe(this, EventObserver { loading ->
            if (isFinishing) return@EventObserver
            if (loading) progress.show() else progress.dismiss()
        })

        authViewModel.message.observe(this, EventObserver { showToast(it, Toast.LENGTH_LONG) })
        authViewModel.dataLoading.observe(this, EventObserver { loading ->
            if (isFinishing) return@EventObserver
            if (loading) progress.show() else progress.dismiss()
        })

        if (savedInstanceState == null) {
            binding.layGoogleSignin.visibility = View.VISIBLE
            binding.layGoogleSignin.toogleTouchable(true)
            replaceFragment(EmailFragment())
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Utils.printIntentExtras(intent)
    }

    override fun onVisibilityChanged(isKeyboardOpen: Boolean) {
    }

    override fun onBackPressed() {
        super.onBackPressed()
        binding.layGoogleSignin.visibility = View.INVISIBLE
        binding.layGoogleSignin.toogleTouchable(false)
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_layout)
        if (fragment is EmailFragment) {
            binding.layGoogleSignin.visibility = View.VISIBLE
            binding.layGoogleSignin.toogleTouchable(true)
        }
    }

    override fun onNext(email: String, password: String, imageUri: Uri?) {
        mViewModel.onEmailEntered(email, password)
        mViewModel.onRegister()
        mViewModel.isLoggedInEvent.observe(this, EventObserver {
            // send email for verification for verification
            authViewModel.sendEmailVerification(it) { goToLoginActivity() }
        })
    }

    private fun replaceFragment(fragment: Fragment, name: String? = null) {
        val transaction = supportFragmentManager.beginTransaction().replace(R.id.frame_layout, fragment)
        if (name.isNullOrEmpty()) {
            transaction.commit()
        } else {
            transaction.addToBackStack(name).commit()
        }
    }

    private fun initSignInWithGoogle(btnGoogleSignIn: View) {
        val mGoogleSignInClient = GoogleSignIn.getClient(this, (application as MyApp).gso)
        btnGoogleSignIn.setOnClickListener {
            val intent = mGoogleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val credential = GoogleAuthProvider.getCredential(task.result?.idToken, null)
                authViewModel.signInWithGoogle(credential)
                authViewModel.signInWithGoogleEvent.observe(this) { goToHomeActivity(it.uid) }
            }
        }
    }

    private fun goToHomeActivity(userID: String) {
        preferences.isLoggedIn(true)
        preferences.loggedUID(userID)
        HomeActivity.start(this)
    }

    companion object {
        private const val TAG = "RegisterActivity"
        private const val RC_SIGN_IN = 11
    }
}