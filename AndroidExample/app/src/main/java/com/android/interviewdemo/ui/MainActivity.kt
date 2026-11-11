package com.android.interviewdemo.ui

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.interviewdemo.R
import com.android.interviewdemo.databinding.ActivityMainBinding
import com.android.interviewdemo.model.models.User
import com.android.interviewdemo.ui.adapter.MainRvAdapter
import com.android.interviewdemo.ui.common.BaseActivity
import com.android.interviewdemo.utils.setLightStatusBar
import com.android.interviewdemo.utils.SingleEvent
import com.android.interviewdemo.utils.goToSettings
import com.android.interviewdemo.utils.observe
import com.android.interviewdemo.utils.observeEvent
import com.android.interviewdemo.utils.registerForPermissionResult
import com.android.interviewdemo.utils.requestPermission
import com.android.interviewdemo.utils.showToast
import com.android.interviewdemo.viewmodel.MainActivityViewModel

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val viewModel by viewModels<MainActivityViewModel>()

    private val recordAudioResult = registerForPermissionResult { isGranted: Boolean ->
        if (isGranted) onPermissionGranted()
        else {
            showToast("Audio recording permission denied! Go to permissions settings")
            goToSettings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightStatusBar(window, true)
        setSupportActionBar(binding.toolbar)
        title = getString(R.string.app_name)

        binding.progressBar.isVisible = true
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        observeEvent(viewModel.openDetailsScreen, ::navigateToDetailsScreen)
        observe(viewModel.getUsers()) {
            binding.progressBar.isVisible = false

            //binding.listView.adapter = CustomListAdapter(this@MainActivity, ArrayList(it))
            binding.recyclerView.adapter = MainRvAdapter(viewModel, ArrayList(it))
            if (it.isEmpty()) showToast("Data not found!")
        }

        requestPermission(Manifest.permission.RECORD_AUDIO, recordAudioResult, this::onPermissionGranted)
    }

    private fun onPermissionGranted() {}

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_actions, menu)
        // Associate searchable configuration with the SearchView
        // https://github.com/ahmedeltaher/MVVM-Kotlin-Android-Architecture/blob/master/app/src/main/java/com/task/ui/component/recipes/RecipesListActivity.kt
        // https://github.com/ahmedeltaher/MVVM-Kotlin-Android-Architecture/
        // https://github.com/akhilesh0707/MusicPlayer/
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> viewModel.getUsers()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToDetailsScreen(navigateEvent: SingleEvent<User>) {
        /*navigateEvent.getContentIfNotHandled()?.let {
            startActivity(Intent(this, DetailsActivity::class.java).apply {
                putExtra(RECIPE_ITEM_KEY, it)
            })
        }*/
    }

}