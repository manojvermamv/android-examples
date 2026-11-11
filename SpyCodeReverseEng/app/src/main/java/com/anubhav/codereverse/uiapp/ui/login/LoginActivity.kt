package com.anubhav.codereverse.uiapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.anubhav.codereverse.databinding.ActivityLoginBinding
import com.anubhav.customprogresshud.CustomProgressHUD
import com.anubhav.codereverse.uiapp.common.EventObserver
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.anubhav.codereverse.uiapp.ui.common.*
import com.anubhav.codereverse.uiapp.ui.home.HomeActivity
import com.anubhav.codereverse.uiapp.ui.register.AuthViewModel
import com.anubhav.codereverse.uiapp.ui.register.RegisterActivity
import com.anubhav.commonutility.GlobalData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.GoogleAuthProvider
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class LoginActivity : BaseActivity<ActivityLoginBinding>(), KeyboardVisibilityEventListener {

    lateinit var progress: CustomProgressHUD
    private val mViewModel: LoginViewModel by viewModels { viewModelFactory() }
    private val authViewModel: AuthViewModel by viewModels { viewModelFactory() }

    override fun getViewBinding() = ActivityLoginBinding.inflate(layoutInflater)

    override fun onCreateApp(savedInstanceState: Bundle?) {
        GlobalData.setStatusBarFullScreen(this)

        KeyboardVisibilityEvent.setEventListener(this, this)
        coordinateBtnAndInputs(binding.loginBtn, binding.emailInput, binding.passwordInput)

        // show progress bar
        progress = getCustomProgress()

        mViewModel.message.observe(this, EventObserver { showToast(it) })
        mViewModel.dataLoading.observe(this, EventObserver { loading ->
            if (isFinishing) return@EventObserver
            if (loading) progress.show() else progress.dismiss()
        })

        authViewModel.message.observe(this, EventObserver { showToast(it, Toast.LENGTH_LONG) })
        authViewModel.dataLoading.observe(this, EventObserver { loading ->
            if (isFinishing) return@EventObserver
            if (loading) progress.show() else progress.dismiss()
        })

        binding.createAccountText.setOnClickListener { goToRegisterActivity() }
        binding.loginBtn.setOnClickListener {
            //createRandomUsers()
            mViewModel.onLoginClick(binding.emailInput.text.toString(), binding.passwordInput.text.toString())
            mViewModel.isLoggedInEvent.observe(this) { goToHomeActivity(it.uid) }
        }
        initSignInWithGoogle(binding.loginWithGoogle)

        // enable auto login
        binding.emailInput.setText("dikshakumari5643@gmail.com")
        binding.passwordInput.setText("78967896")
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
                val result = task.result!!
                //val email = result.account?.name!!
                val credential = GoogleAuthProvider.getCredential(result.idToken, null)
                authViewModel.signInWithGoogle(credential)
                authViewModel.signInWithGoogleEvent.observe(this) { goToHomeActivity(it.uid) }
            }
        }
    }

    override fun onVisibilityChanged(isKeyboardOpen: Boolean) {
        if (isKeyboardOpen) {
            binding.tvFooter.visibility = View.GONE
            binding.loginWithGoogle.visibility = View.GONE
        } else {
            binding.tvFooter.visibility = View.VISIBLE
            binding.loginWithGoogle.visibility = View.VISIBLE
        }
    }

    private fun goToRegisterActivity() {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }

    private fun goToHomeActivity(userID: String) {
        preferences.isLoggedIn(true)
        preferences.loggedUID(userID)
        HomeActivity.start(this)
    }

    companion object {
        private const val RC_SIGN_IN = 11
    }

    private fun appendLog(text: String) {
        val logFile = File("sdcard/log.file")
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append(text)
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}