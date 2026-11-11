package com.anubhav.codereverse.uiapp.ui.socialengineering

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.anubhav.codereverse.R
import com.anubhav.codereverse.databinding.ActivitySendsmsBinding
import com.anubhav.codereverse.uiapp.dialogs.BsSendSmsFragment
import com.anubhav.codereverse.uiapp.ui.common.*
import com.anubhav.commonutility.GlobalData
import com.anubhav.customprogresshud.CustomProgressHUD
import com.google.gson.Gson
import java.net.URL

class SendSMSActivity : BaseActivity<ActivitySendsmsBinding>(), BsSendSmsFragment.Listener {

    private lateinit var progress: CustomProgressHUD

    private var smsServiceUsernameKey = ""
    private var smsServicePasswordKey = ""
    private var smsServiceTokenKey = ""
    private var smsServiceType = SmsServiceType.TO_FACTOR

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SendSMSActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getViewBinding() = ActivitySendsmsBinding.inflate(layoutInflater)

    override fun onCreateApp(savedInstanceState: Bundle?) {
        // show progress bar
        progress = getCustomProgress()

        binding.layActionbar.actionBarTitle = "SMS Sender"
        binding.layActionbar.imgBack.setOnSingleClickListener { finish() }

        binding.rbTextbelt.setOnLongClickListener {
            showToast("Use Default Api-Key: textbelt \nTo send 1 free text per day", Toast.LENGTH_LONG)
            return@setOnLongClickListener false
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            setSmsService(group.checkedRadioButtonId)
            when (group.checkedRadioButtonId) {
                R.id.rbTextbelt -> {
                    smsServiceType = SmsServiceType.TEXT_BELT
                    binding.inputUsername.visibility = View.GONE
                    binding.inputPassword.visibility = View.GONE
                    binding.inputToken.visibility = View.VISIBLE
                }
                R.id.rbToFactor -> {
                    smsServiceType = SmsServiceType.TO_FACTOR
                    binding.inputUsername.visibility = View.GONE
                    binding.inputPassword.visibility = View.GONE
                    binding.inputToken.visibility = View.VISIBLE
                }
                R.id.rbWayToSms -> {
                    smsServiceType = SmsServiceType.WAY_TO_SMS
                    binding.inputUsername.visibility = View.VISIBLE
                    binding.inputPassword.visibility = View.VISIBLE
                    binding.inputToken.visibility = View.VISIBLE
                }
                R.id.rbFastToSms -> {
                    smsServiceType = SmsServiceType.FAST_TO_SMS
                    binding.inputUsername.visibility = View.GONE
                    binding.inputPassword.visibility = View.GONE
                    binding.inputToken.visibility = View.VISIBLE
                }
                R.id.rbBlacktelIO -> {
                    smsServiceType = SmsServiceType.BLACKTEL_IO
                    binding.inputUsername.visibility = View.GONE
                    binding.inputPassword.visibility = View.GONE
                    binding.inputToken.visibility = View.GONE
                }
                else -> {}
            }
        }

        binding.btnSendSms.setOnSingleClickListener {
            if (smsServiceType == SmsServiceType.BLACKTEL_IO) {
                GlobalData.openWebPage(this, smsServiceType.baseUrl)
            } else {
                val username = preferences.pref.getString(smsServiceUsernameKey, "") ?: ""
                val password = preferences.pref.getString(smsServicePasswordKey, "") ?: ""
                val token = preferences.pref.getString(smsServiceTokenKey, "") ?: ""
                BsSendSmsFragment.show(supportFragmentManager, smsServiceType, username, password, token)
            }
        }

        binding.inputUsername.setOnRightDrawableClickListener {
            showConfirmDialog("Info", "Insure add a Email or Username for SMS Service account authentication.")
        }
        binding.inputPassword.setOnRightDrawableClickListener {
            showConfirmDialog("Info", "Insure add a Password for SMS Service account authentication.")
        }
        binding.inputToken.setOnRightDrawableClickListener {
            showConfirmDialog("Info", "Insure add a Api key or Token for sms authrizatuion.")
        }

        binding.rbTextbelt.isChecked = true
        binding.radioGroup.check(R.id.rbTextbelt)
    }

    private fun setSmsService(checkedId: Int) {
        smsServiceUsernameKey = "smsServiceUsername$checkedId"
        smsServicePasswordKey = "smsServicePassword$checkedId"
        smsServiceTokenKey = "smsServiceToken$checkedId"

        binding.inputUsername.setText(preferences.pref.getString(smsServiceUsernameKey, ""))
        binding.inputUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isNotEmpty()) {
                    preferences.pref.edit().putString(smsServiceUsernameKey, s.toString()).apply()
                }
            }
        })

        binding.inputPassword.setText(preferences.pref.getString(smsServicePasswordKey, ""))
        binding.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isNotEmpty()) {
                    preferences.pref.edit().putString(smsServicePasswordKey, s.toString()).apply()
                }
            }
        })

        binding.inputToken.setText(preferences.pref.getString(smsServiceTokenKey, ""))
        binding.inputToken.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isNotEmpty()) {
                    preferences.pref.edit().putString(smsServiceTokenKey, s.toString()).apply()
                }
            }
        })
    }

    private fun getIpResponse(inputIp: String) {
        lifecycleScope.executeAsyncTask(onPreExecute = {
            progress.show()
        }, doInBackground = {
            val url = preferences.ipFinderApiUrl().replace("<IP>", inputIp)
            val response = URL(url).readText()
            try {
                Gson().fromJson(response, IpResponse::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                IpResponse(false)
            }
        }, onPostExecute = { ipResponse ->
            if (ipResponse == null || !ipResponse.status) {
                showToast("Please ensure you've entered your token correctly or An error occurred.")
            } else {
                progress.dismiss()
            }
        })
    }

    override fun onSent(success: Boolean, message: String) {
        if (message.isNotEmpty()) showToast(message, Toast.LENGTH_LONG)
    }

}

enum class SmsServiceType(val baseUrl: String) {
    TEXT_BELT("https://www.textbelt.com/"),
    TO_FACTOR("https://www.2factor.in/"),
    WAY_TO_SMS("https://www.way2sms.com/"),
    FAST_TO_SMS("https://www.fast2sms.com/"),
    BLACKTEL_IO("https://www.blacktel.io/en/free"),
}