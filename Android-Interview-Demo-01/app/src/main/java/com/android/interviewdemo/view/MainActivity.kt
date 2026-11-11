package com.android.interviewdemo.view

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.android.interviewdemo.databinding.ActivityMainBinding
import com.android.interviewdemo.view.common.BaseActivity
import com.android.interviewdemo.viewmodel.MainActivityViewModel

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        binding.progressBar.isVisible = true
        viewModel.getUsers().observe(this) { users ->
            binding.progressBar.isVisible = false

            var mPrice = 0
            users.onEach { mPrice += it.getPrice() }
            viewModel.setPrice(mPrice)

            binding.listView.adapter = CustomListAdapter(this@MainActivity, ArrayList(users), viewModel::updatePrice)
        }

        viewModel.totalPrice.observe(this) {
            binding.tvTotalPrice.text = "$it rs"
        }
    }

}