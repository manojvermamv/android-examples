package com.anubhav.codereverse.uiapp.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableItem
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import com.anubhav.codereverse.BuildConfig
import com.anubhav.codereverse.R
import com.anubhav.codereverse.databinding.ActivityHomeBinding
import com.anubhav.codereverse.uiapp.common.IConstants.UPDATE_DOMAIN_CONFIG
import com.anubhav.codereverse.uiapp.database.firebase.entity.UserInfo
import com.anubhav.codereverse.uiapp.database.preferences.PrefParam
import com.anubhav.codereverse.uiapp.database.retrofit.entity.PhishingConfig
import com.anubhav.codereverse.uiapp.database.retrofit.remote.RetrofitBuilder
import com.anubhav.codereverse.uiapp.database.retrofit.remote.addCallback
import com.anubhav.codereverse.uiapp.database.retrofit.remote.parseResponse
import com.anubhav.codereverse.uiapp.ui.common.*
import com.anubhav.codereverse.uiapp.ui.myprofile.UsersViewModel
import com.anubhav.codereverse.uiapp.ui.notifications.NotificationsActivity
import com.anubhav.codereverse.uiapp.ui.socialengineering.IpFinderActivity
import com.anubhav.codereverse.uiapp.ui.socialengineering.MaskingUrlActivity
import com.anubhav.codereverse.uiapp.ui.socialengineering.PhishingActivity
import com.anubhav.codereverse.uiapp.ui.socialengineering.SendSMSActivity
import com.anubhav.codereverse.uiapp.ui.webservice.showWebViewLoadingDialog
import com.anubhav.codereverse.uiapp.utils.Utils
import com.anubhav.commonutility.FileUtils
import com.anubhav.commonutility.GlobalData
import com.anubhav.commonutility.ScreenshotUtil
import com.anubhav.customprogresshud.CustomProgressHUD
import com.chillibits.simplesettings.core.SimpleSettings
import com.chillibits.simplesettings.core.SimpleSettingsConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import java.util.*

// App Debugging : https://medium.com/mobile-app-development-publication/debug-without-using-temporary-log-in-android-studio-91d9394a1e94
class HomeActivity : BaseActivity<ActivityHomeBinding>(), Drawer.OnDrawerItemClickListener {

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, HomeActivity::class.java))
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            activity.finish()
        }
    }

    private val userViewModel: UsersViewModel by viewModels { viewModelFactory() }
    private val homeViewModel: HomeViewModel by viewModels { viewModelFactory() }

    private lateinit var mUser: UserInfo
    private lateinit var progress: CustomProgressHUD

    var phishingSubDomains = mutableListOf<String>()
    var socialEnggineeringItems = mutableListOf("IP Finder", "Masking Url", "SMS Sender")
    var permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val requestCode = 1

    override fun getViewBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun onCreateApp(savedInstanceState: Bundle?) {
        progress = getCustomProgress()
        //progress.show()

        binding.layActionbar.actionBarTitle = getString(R.string.app_name)
        registerForNotificationToken()
        askPermissions()

        if (homeViewModel.firebaseUser != null) {
            userViewModel.user.observe {
                mUser = it.info
            }

            userViewModel.onSharedProgressBar.observe { show ->
                if (show) progress.show() else progress.dismiss()
            }
        }

        //MobileAds.initialize(this)
        val baseUrl = preferences.domainBaseUrl()
        if (baseUrl.isNotEmpty() && baseUrl.startsWith("http")) {
            binding.rootView.delayOnLifecycle(800) { updatePhishingConfig() }
        } else {
            showToast("First configure domain base url in settings")
        }
    }

    override fun onStart() {
        super.onStart()
        preferences.readLiveData(PrefParam.PHISHING_ADD_SUBDOMAIN, "", this) {
            phishingSubDomains.clear()
            for (item in it.trim().split("\n")) {
                if (item.isNotEmpty()) phishingSubDomains.add(item)
            }
            initDrawer()
        }
    }

    /**
     * Register your FCM token to receive incoming notification
     */
    private fun registerForNotificationToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val fcmToken: String = Objects.requireNonNull(token)
            preferences.fcmToken(fcmToken)
            userViewModel.uploadFCMToken(token = fcmToken)
        }
    }

    private fun updatePhishingConfig() {
        val config = Gson().toJson(PhishingConfig(preferences.fcmServerKey(), preferences.fcmToken(), "https://api.allorigins.win/get?url="))
        if (preferences.canRequestWithWebView()) {
            showWebViewLoadingDialog("${preferences.domainBaseUrl()}$UPDATE_DOMAIN_CONFIG", config) { dialog, json ->
                if (json.has("status")) {
                    showToast(json.optString("message"))
                }
                dialog?.dismiss()
            }
        } else {
            progress.show()
            RetrofitBuilder.create(this).updateConfig(config).addCallback { isSuccessful, response ->
                if (isSuccessful) {
                    val json = response.parseResponse()
                    if (json.has("status")) {
                        showToast(json.optString("message"))
                    }
                }
                progress.dismiss()
            }
        }
    }

    fun askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    fun isPermissionsGranted(): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    fun saveImageFromTextView() {
        val textView = binding.tvAppLogo
        textView.visibility = View.VISIBLE
        GlobalData.setGradientTextColor(textView, intArrayOf(
            ContextCompat.getColor(this, R.color.colorAccent), ContextCompat.getColor(this, R.color.colorAccent)))

        val bitmap = ScreenshotUtil.getInstance().takeScreenshotForView(textView)
        FileUtils.saveBitmapToFile(this, bitmap)
    }

    /**
     * Drawer screen
     * */
    private fun initDrawer() {
        val drawer = drawer {
            accountHeader {
                backgroundDrawable = ColorDrawable(Color.WHITE)
                profile("Manoj Verma", "manojverma@gmail.com") {
                    icon = R.mipmap.img_avatar
                }
            }

            primaryItem("Home")
            expandableItem("Phishing") {
                selectable = false
                for (item in phishingSubDomains) {
                    primaryItem(item) {
                        tag = item
                        selectable = false
                    }
                }
            }
            expandableItem("Social Engineering") {
                selectable = false
                for (item in socialEnggineeringItems) {
                    primaryItem(item) {
                        tag = item
                        selectable = false
                    }
                }
            }
            primaryItem("Notifications") {
                tag = "Notifications"
                selectable = false
            }

            stickyFooterShadow = false
            footer {
                divider {}
                primaryItem("Settings") {
                    tag = "Settings"
                    icon = R.drawable.ic_round_settings_24
                    selectable = false
                }
                primaryItem("About Us") {
                    tag = "About Us"
                    icon = R.drawable.ic_outline_info_24
                    selectable = false
                }
                secondaryItem("Version: ${BuildConfig.VERSION_NAME}") {
                    enabled = false
                }
            }

            // Called when any drawer item is clicked
            builder.withOnDrawerItemClickListener(this@HomeActivity)
        }

        binding.layActionbar.imgNavmenu.setOnSingleClickListener {
            drawer.drawerLayout.openDrawer(Gravity.START)
        }
        binding.layActionbar.btnSettings.setOnSingleClickListener { openSetting() }
    }

    override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
        if (drawerItem.tag is String && (drawerItem.tag as String).isNotEmpty()) {
            val context = this@HomeActivity
            when (val tag = drawerItem.tag as String) {
                "Settings" -> { openSetting() }
                "About Us" -> { }
                "Notifications" -> { NotificationsActivity.start(context) }
                else -> {
                    if (phishingSubDomains.contains(tag)) {
                        PhishingActivity.start(context, tag)
                    } else if (socialEnggineeringItems[0] == tag) {
                        IpFinderActivity.start(context, "")
                    } else if (socialEnggineeringItems[1] == tag) {
                        MaskingUrlActivity.start(context)
                    } else if (socialEnggineeringItems[2] == tag) {
                        SendSMSActivity.start(context)
                    }
                }
            }
        }
        return false
    }

    /**
     * Settings screen
     * */
    private fun openSetting() {
        //SettingsActivity.start(this)
        val config = SimpleSettingsConfig().apply {
            activityTitle = "Settings"
            showResetOption = false
            iconSpaceReservedByDefault = false
        }

        SimpleSettings(this, config).show {
            Section {
                title = "General"
                CheckboxPref {
                    title = PrefParam.REQUEST_WITH_WEBVIEW.value
                    key = PrefParam.REQUEST_WITH_WEBVIEW.key
                    summary = "Enable custom webview requests\nNote: use for these server only support javascript requests"
                    defaultValue = false
                }
                Page {
                    title = "Social Engineering"
                    summary = "Configure social engineering settings"
                    activityTitle = "Social Engineering"
                    Section {
                        InputPref {
                            title = PrefParam.SE_IP_FINDER_API.value
                            key = PrefParam.SE_IP_FINDER_API.key
                            summary = "Configure IP Finder Api. Ex: https://ipinfo.io/<IP>?token=<YOUR_TOKEN>"
                            defaultValue = "https://ipinfo.io/<IP>?token=4bcc1359b5f63f"
                        }
                        InputPref {
                            title = PrefParam.SE_SHORTING_API.value
                            key = PrefParam.SE_SHORTING_API.key
                            summary = "Configure Link Shorting Api urls. Ex: https://tinyurl.com/api-create.php?url="
                            defaultValue = "https://is.gd/create.php?format=simple&url="
                        }
                        DropDownPref {
                            title = "DropDownPreference"
                            simpleSummaryProvider = true
                            entries = listOf("Apple", "Banana", "Avocado", "Pineapple")
                        }
                    }
                }
            }
            Section {
                title = "Firebase"
                InputPref {
                    title = PrefParam.FCM_SERVER_KEY.value
                    key = PrefParam.FCM_SERVER_KEY.key
                    summary = "Configure Firebase server key\nYou can update server key to get realtime notifications"
                    defaultValue = BuildConfig.FCM_SERVER_KEY
                }
            }
            Section {
                title = "Phishing"
                InputPref {
                    title = PrefParam.DOMAIN_BASE_URL.value
                    key = PrefParam.DOMAIN_BASE_URL.key
                    summary = "Configure base domain url"
                    defaultValue = "http://yourdomain.xyz"
                }
                InputPref {
                    title = PrefParam.PHISHING_ADD_SUBDOMAIN.value
                    key = PrefParam.PHISHING_ADD_SUBDOMAIN.key
                    summary = "Configure Sub Domain urls for phishing sites\nNote. add only single domain at single line & don't add duplicate domains"
                    defaultValue = "instagram.com"
                }
            }
            Section {
                title = "Advanced"
                TextPref {
                    title = "Clear App Cache"
                    summary = "Clear all cache files of application"
                    onClick = Preference.OnPreferenceClickListener {
                        //context.showConfirmDialog("Confirm", "Are you ensure continue to exit from application and restart") {
                        if (context.cacheDir.deleteRecursively())
                            Utils.restartApp(context)
                        //}
                        true
                    }
                }
            }
        }
    }

}

fun Activity.setTextFromWebView(view: TextView, text: String) {
    runOnUiThread {
        view.text = text
        view.isSelected = true
    }
}

fun Activity.setErrorFromWebView(view: TextView, text: String) {
    val sb = StringBuilder()
    if (text.isNotEmpty() && text.length > 100) {
        sb.append("Response: ")
        sb.append(text.substring(text.length - 60, text.length))
    } else if (text.isNotEmpty()) {
        sb.append("Response: ")
        sb.append(text)
    }
    runOnUiThread {
        view.text = sb.toString()
        view.isSelected = true
    }
}