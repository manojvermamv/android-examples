package com.anubhav.codereverse.uiapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anubhav.codereverse.R
import com.anubhav.codereverse.uiapp.ui.home.HomeActivity
import com.anubhav.codereverse.uiapp.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().currentUser.let {
            if (it != null) {
                startActivity(Intent(this, HomeActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }
    }

}