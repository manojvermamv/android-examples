package com.android.interviewdemo.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class BaseViewHolder<VB : ViewBinding>(val binding: VB) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        inline fun <VB : ViewBinding> create(
            parent: ViewGroup,
            crossinline block: (inflater: LayoutInflater, container: ViewGroup, attach: Boolean) -> VB
        ) = BaseViewHolder(block(LayoutInflater.from(parent.context), parent, false))
    }
}