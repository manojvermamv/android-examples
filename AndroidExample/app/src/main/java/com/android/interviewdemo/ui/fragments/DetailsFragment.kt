package com.android.interviewdemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.android.interviewdemo.databinding.FragmentDetailsBinding
import com.android.interviewdemo.ui.common.BaseFragment
import com.android.interviewdemo.viewmodel.NavActivityViewModel

class DetailsFragment : BaseFragment<FragmentDetailsBinding>(FragmentDetailsBinding::inflate) {

    private val viewModel by activityViewModels<NavActivityViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getString("id", "") ?: ""
        binding.txtView.text = "This is final Screen, $uid"
        binding.button.setOnClickListener {
            viewModel.backToHomeScreen()
        }
    }

}