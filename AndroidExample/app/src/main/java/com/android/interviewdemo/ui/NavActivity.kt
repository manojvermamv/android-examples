package com.android.interviewdemo.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.android.interviewdemo.R
import com.android.interviewdemo.databinding.ActivityNavBinding
import com.android.interviewdemo.ui.common.BaseActivity
import com.android.interviewdemo.utils.SingleEvent
import com.android.interviewdemo.utils.observe
import com.android.interviewdemo.utils.observeEvent
import com.android.interviewdemo.utils.setLightStatusBar
import com.android.interviewdemo.viewmodel.NavActivityViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NavActivity : BaseActivity<ActivityNavBinding>(ActivityNavBinding::inflate) {

    private val viewModel by viewModels<NavActivityViewModel>()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightStatusBar(window, true)
        setSupportActionBar(binding.toolbar)
        title = getString(R.string.app_name)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

        observeEvent(viewModel.openHomeScreen, ::navigateToHomeScreen)
        observeEvent(viewModel.openDetailsScreen, ::navigateToDetailsScreen)
        observe(viewModel.backToHomeScreen, ::navigateBackToHomeScreen)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /*
    private fun navHostController() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }
    }
    * */

    private fun navigateToHomeScreen(navigateEvent: SingleEvent<String>) {
        title = "Home"
        navigateEvent.getContentIfNotHandled()?.let {
            binding.progressBar.isVisible = true
            binding.frameLayoutContainer.isVisible = false
            lifecycleScope.launch {
                delay(2000)
                binding.progressBar.isVisible = false
                binding.frameLayoutContainer.isVisible = true
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    navController.navigate(R.id.action_loginFragment_to_homeFragment, Bundle().apply {
                        putString("id", it)
                    })
                }
            }
        }
    }

    private fun navigateToDetailsScreen(navigateEvent: SingleEvent<String>) {
        title = "Details"
        navigateEvent.getContentIfNotHandled()?.let {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                navController.navigate(R.id.action_homeFragment_to_detailsFragment, Bundle().apply {
                    putString("id", it)
                })
            }
        }
    }

    private fun navigateBackToHomeScreen(navigateEvent: Unit) {
        title = "Home"
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            navController.navigate(R.id.action_detailsFragment_to_homeFragment)
        }
    }

}