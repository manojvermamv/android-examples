package com.android.interviewdemo.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.android.interviewdemo.R
import com.android.interviewdemo.databinding.ActivityMainBinding
import com.android.interviewdemo.ui.common.BaseActivity
import com.android.interviewdemo.ui.common.GlobalAction
import com.android.interviewdemo.ui.common.UiAction
import com.android.interviewdemo.ui.common.ViewPagerProvider
import com.android.interviewdemo.ui.common.ViewPagerProviderImpl
import com.android.interviewdemo.ui.dialogs.CustomMaterialDialog
import com.android.interviewdemo.utils.NetworkConnection
import com.android.interviewdemo.utils.SingleEvent
import com.android.interviewdemo.utils.awaitLayoutChange
import com.android.interviewdemo.utils.goToSettings
import com.android.interviewdemo.utils.isAllPermissionsGranted
import com.android.interviewdemo.utils.observeEvent
import com.android.interviewdemo.utils.onBackPressed
import com.android.interviewdemo.utils.registerForMultiplePermissionsResult
import com.android.interviewdemo.utils.setGradientTextColor
import com.android.interviewdemo.utils.setLightStatusBar
import com.android.interviewdemo.utils.showToast
import com.android.interviewdemo.viewmodel.MainActivityViewModel
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.system.exitProcess


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    ViewPagerProvider by ViewPagerProviderImpl() {

    private var keepSplashAlive = true
    private val viewModel by viewModel<MainActivityViewModel>()

    private val noInternetDialog by lazy {
        CustomMaterialDialog(this).apply {
            setOnConfirmClickListener { exitProcess(0) }
            setOnDismissListener {
                viewModel.userDataRes.refresh()
                viewModel.featuredTournamentsRes.resetData()
                viewModel.upcomingMatchesRes.resetData()
            }
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val set = ConstraintSet()
            binding.bgView.apply {
                layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                    matchConstraintPercentHeight = if (position == 0) 0.79f else 0.99f
                }
            }
            set.applyTo(binding.constraintLayout)
        }
    }

    private val relationalMsg = "This app needs notification permissions to get updates."
    private val permissions by lazy {
        mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toList()
    }
    private val permissionsLauncher = registerForMultiplePermissionsResult { results ->
        if (results.isAllPermissionsGranted().not()) {
            goToSettings()
            showToast("This app needs notification permissions! Go to settings and grant all permissions.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        //Keep returning false to Should Keep On Screen until ready to begin.
        splashScreen.setKeepOnScreenCondition { keepSplashAlive }

        // Will cause a second process to run on the main thread
        CoroutineScope(Dispatchers.Main).launch {
            delay(200L)
            authenticateUser()
            keepSplashAlive = false
        }

        setLightStatusBar(window, true)
        setSupportActionBar(binding.toolbar)
        onBackPressed {
            if (binding.viewPager.currentItem != 0) {
                binding.viewPager.setCurrentItem(0, false)
            } else {
                finish()
            }
            true
        }

        setupViews()
        updateTextWithGradient()
        registerViewPagerAdapter(this, this, supportFragmentManager)
        setupBottomNavigation(binding.viewPager, binding.tabLayout, onPageChangeCallback)

        observeActions()
        observeEvent(viewModel.progressBar, ::triggerProgressBar)
        //requestPermission(permissions, relationalMsg, permissionsLauncher)

        NetworkConnection(applicationContext).observe(this) { isConnected ->
            noInternetDialog.apply {
                binding.viewPager.isInvisible = !isConnected
                if (isConnected) dismiss() else show()
            }
        }
        setupOptionChain()
    }

    private fun authenticateUser() {}

    private fun setupViews() {
        binding.ivMenu.setOnClickListener {}
        binding.layBalance.setOnClickListener {}
        binding.ivNotification.setOnClickListener {}
        binding.tvName.text = getString(R.string.hi_developer)
    }

    private fun updateTextWithGradient(balance: Int? = null) {
        binding.tvBalance.text = (balance ?: 0).toString()
        lifecycle.coroutineScope.launch {
            binding.tvBalancePrefix.awaitLayoutChange()
            binding.tvBalancePrefix.setGradientTextColor(R.color.accent_start, R.color.accent_end)
            binding.tvBalance.awaitLayoutChange()
            binding.tvBalance.setGradientTextColor(R.color.accent_start, R.color.accent_end)
        }
    }

    private fun observeActions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.globalAction.collect { action ->
                    when (action) {
                        is GlobalAction.MenuClick -> showToast("Menu Clicked")
                        is GlobalAction.WalletClick -> showToast("Wallet Clicked")
                        is GlobalAction.NotificationClick -> showToast("Notification Clicked")
                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiAction.collect { action ->
                when (action) {
                    is UiAction.UpdateWallet -> updateTextWithGradient(action.data.cashBalance)
                    else -> Unit
                }
            }
        }
    }

    private fun triggerProgressBar(event: SingleEvent<Boolean>) {
        event.getContentIfNotHandled()?.let {
            binding.progressBar.isVisible = it
        }
    }

    private var nseOptionChain: NSEOptionChain? = null

    private fun setupOptionChain() {
        if (nseOptionChain == null) {
            nseOptionChain = NSEOptionChain()
        }
        binding.fetchButton.setOnClickListener { testApiConnections() }
    }

    @SuppressLint("SetTextI18n")
    private fun testApiConnections() {
        binding.statusText.text = "Fetching data..."
        lifecycleScope.launch {
            try {
                val testResult = nseOptionChain?.testApiResponses()
                Log.d("MainActivity", "API Test Results:\n$testResult")

                // If test passes, proceed with actual data fetch
                if (testResult?.contains("SUCCESS", true) == true) {
                    binding.statusText.text = "Data fetch complete. Check logs and CSV file."
                    fetchDataOnce()
                }
            } catch (e: Exception) {
                binding.statusText.text = "API test failed. \n${e.message}"
                Log.e("MainActivity", "API test failed", e)
            }
        }
    }

    private fun fetchDataOnce() {
        lifecycleScope.launch {
            try {
                Log.d("MainActivity", "Starting data fetch...")
                nseOptionChain?.fetchCurrentData()?.let { (niftyData, bankNiftyData) ->
                    Log.d("MainActivity", "Data fetch successful!")
                    // Update UI with fetched data
                    updateUI(niftyData, bankNiftyData)
                } ?: run {
                    Log.e("MainActivity", "Data fetch returned null")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in fetchDataOnce", e)
            }
        }
    }

    private fun updateUI(niftyData: List<NSEOptionChain.OIData>, bankNiftyData: List<NSEOptionChain.OIData>) {
        // Example UI updates
        Log.e("NSEOptionChain", "Received ${niftyData.size} Nifty OI records")
        Log.e("NSEOptionChain", "Received ${bankNiftyData.size} Bank Nifty OI records")

        println("============================================>")
        println(Gson().toJson(niftyData))
        println(Gson().toJson(bankNiftyData))
        println("============================================>")

        // You can update RecyclerView, TextViews, Charts etc. here
        // For example:
        // niftyAdapter.updateData(niftyData)
        // bankNiftyAdapter.updateData(bankNiftyData)
    }

}