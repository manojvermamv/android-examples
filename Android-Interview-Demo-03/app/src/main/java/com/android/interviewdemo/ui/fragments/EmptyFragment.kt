package com.android.interviewdemo.ui.fragments

import android.os.Bundle
import android.view.View
import com.android.interviewdemo.databinding.FragmentEmptyBinding
import com.android.interviewdemo.ui.common.BaseFragment

class EmptyFragment : BaseFragment<FragmentEmptyBinding>(FragmentEmptyBinding::inflate) {

    private val titleResId: Int? by lazy { arguments?.getInt("titleResId") }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleResId?.let {
            binding.tvTitle.text = getString(it)
        }
    }

    companion object {
        // Get New Instance of Fragment
        fun get(titleResId: Int): EmptyFragment {
            val fragment = EmptyFragment()
            fragment.arguments = Bundle().apply {
                putInt("titleResId", titleResId)
            }
            return fragment
        }
    }
}