package com.anubhav.codereverse.uiapp.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.anubhav.codereverse.databinding.ActivitySettingsBinding
import com.anubhav.codereverse.uiapp.ui.common.*
import com.anubhav.customprogresshud.CustomProgressHUD


class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    private lateinit var progress: CustomProgressHUD

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getViewBinding() = ActivitySettingsBinding.inflate(layoutInflater)

    override fun onCreateApp(savedInstanceState: Bundle?) {
        // show progress bar
        progress = getCustomProgress()

        binding.layActionbar.actionBarTitle = "Settings"
        binding.layActionbar.imgBack.setOnSingleClickListener { finish() }
    }

}