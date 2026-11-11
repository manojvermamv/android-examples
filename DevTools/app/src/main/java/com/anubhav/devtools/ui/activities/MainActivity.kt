package com.anubhav.devtools.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.anubhav.devtools.R
import com.anubhav.devtools.databinding.ActivityMainBinding
import com.anubhav.devtools.model.models.Tool
import com.anubhav.devtools.model.models.ToolNames
import com.anubhav.devtools.service.MainService
import com.anubhav.devtools.ui.adapter.ToolRvAdapter
import com.anubhav.devtools.ui.common.BaseActivity
import com.anubhav.devtools.utils.SingleEvent
import com.anubhav.devtools.utils.captureElementByClass
import com.anubhav.devtools.utils.goToSettings
import com.anubhav.devtools.utils.isAllPermissionsGranted
import com.anubhav.devtools.utils.observe
import com.anubhav.devtools.utils.observeEvent
import com.anubhav.devtools.utils.onBackPressed
import com.anubhav.devtools.utils.registerForMultiplePermissionsResult
import com.anubhav.devtools.utils.requestPermission
import com.anubhav.devtools.utils.setDesktopMode
import com.anubhav.devtools.utils.setLightStatusBar
import com.anubhav.devtools.utils.setPopupMenuList
import com.anubhav.devtools.utils.showToast
import com.anubhav.devtools.viewmodel.MainActivityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.system.exitProcess


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate), MenuProvider {

    private val viewModel by viewModel<MainActivityViewModel>()

    private var isServiceBound = false
    private var mainService: MainService? = null
    private var serviceIntent: Intent? = null

    private var isActivityPaused: Boolean = false

    private val relationalMsg = "This app needs notifications and media permissions to play music."
    private val permissions by lazy {
        mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            add(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            )
        }.toList()
    }

    private val permissionsLauncher = registerForMultiplePermissionsResult { results ->
        if (results.isAllPermissionsGranted()) {
            onPermissionGranted()
        } else {
            goToSettings()
            showToast("This app needs notifications and media permissions! Go to settings and grant all permissions.")
        }
    }

    private val mainConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MainService.LocalBinder
            isServiceBound = true
            mainService = binder.getService
            viewModel.refreshTools()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightStatusBar(window, true)
        setSupportActionBar(binding.toolbar)
        addMenuProvider(this, this, Lifecycle.State.RESUMED)
        onBackPressed {
            if (binding.recyclerView.isVisible.not()) {
                navigateBack()
            } else {
                finish()
            }
            true
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)

        observeEvent(viewModel.triggerProgressBar, ::triggerProgressBar)
        observe(viewModel.toolsLiveData, this::onLoadTools)
        requestPermission(permissions, relationalMsg, permissionsLauncher, this::onPermissionGranted)
    }

    override fun onStart() {
        super.onStart()
        // setController()
        if (serviceIntent == null) {
            serviceIntent = Intent(this, MainService::class.java).apply { action = MainService.Action.START.name }
            bindService(serviceIntent!!, mainConnection, BIND_AUTO_CREATE)
            startService(serviceIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isActivityPaused) {
            //setController()
            isActivityPaused = false
        }
    }

    override fun onPause() {
        super.onPause()
        isActivityPaused = true
    }

    override fun onDestroy() {
        stopService(serviceIntent)
        mainService = null
        super.onDestroy()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_actions, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> viewModel.refreshTools()
            R.id.action_stop -> stopService()
        }
        return false
    }

    private fun stopService() {
        startService(Intent(this, MainService::class.java).apply { action = MainService.Action.STOP.name })
    }

    private fun exitService() {
        stopService(serviceIntent)
        mainService = null
        exitProcess(0)
    }

    private fun triggerProgressBar(event: SingleEvent<Boolean>) {
        event.getContentIfNotHandled()?.let {
            binding.progressBar.isVisible = it
        }
    }

    private fun onLoadTools(list: List<Tool>) {
        mainService?.setList(ArrayList(list))
        binding.recyclerView.adapter = ToolRvAdapter(ArrayList(list)) { index, tool ->
            when (ToolNames.valueOf(tool.name)) {
                ToolNames.KOTLIN_PLAYGROUND -> kotlinPlay()
                else -> navigateBack()
            }
        }
    }

    private fun onPermissionGranted() {
        viewModel.refreshTools()
    }

    private fun navigateBack() {
        setDisplayHomeAsUp(false)
        binding.recyclerView.isVisible = true
        binding.layScreenCapture.isVisible = false
    }

    /**
     * Kotlin Playground
     * */

    private val menusKotlinPlay = mutableListOf("Enable Desktop Mode", "Disable Desktop Mode")

    @SuppressLint("SetJavaScriptEnabled")
    private fun kotlinPlay() {
        setDisplayHomeAsUp(true)
        binding.recyclerView.isVisible = false
        binding.layScreenCapture.isVisible = true

        WebView.setWebContentsDebuggingEnabled(true)
        binding.webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.loadUrl("javascript:(function(){var meta=document.createElement('meta');meta.name='viewport';meta.content='width=device-width, initial-scale=1.0';document.getElementsByTagName('head')[0].appendChild(meta);})()")
                    view?.evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));", null)
                }
            }
            loadUrl("https://play.kotlinlang.org/")
            settings.setSupportZoom(true)
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            isScrollbarFadingEnabled = false
        }

        binding.btnShare.setOnClickListener {
            // executable-fragment-wrapper, CodeMirror-sizer, CodeMirror-lines
            binding.webView.captureElementByClass("executable-fragment-wrapper")
        }
        binding.btnInfo.setOnClickListener {
            setPopupMenuList(menusKotlinPlay, binding.btnInfo) {
                when (it) {
                    menusKotlinPlay[0] -> binding.webView.setDesktopMode(true)
                    menusKotlinPlay[1] -> binding.webView.setDesktopMode(false)
                    else -> Unit
                }
            }.show()
        }
    }

}