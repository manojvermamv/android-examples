package com.android.interviewdemo.ui.fragments

import android.R.attr.defaultValue
import android.R.attr.key
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.android.interviewdemo.databinding.FragmentHomeBinding
import com.android.interviewdemo.ui.common.BaseFragment
import com.android.interviewdemo.viewmodel.NavActivityViewModel


class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel by activityViewModels<NavActivityViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getString("id", "") ?: ""
        binding.txtView.text = "Welcome Back, $uid"
        binding.button.setOnClickListener {
            viewModel.openDetailsScreen(uid)
        }
    }

}