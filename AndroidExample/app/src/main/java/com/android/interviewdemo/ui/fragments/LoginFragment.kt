package com.android.interviewdemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.android.interviewdemo.databinding.FragmentLoginBinding
import com.android.interviewdemo.ui.common.BaseFragment
import com.android.interviewdemo.viewmodel.NavActivityViewModel

class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    private val viewModel by activityViewModels<NavActivityViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener {
            viewModel.openHomeScreen("My Unique Id")
        }
    }

}