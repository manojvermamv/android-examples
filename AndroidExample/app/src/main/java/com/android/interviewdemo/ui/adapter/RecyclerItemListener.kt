package com.android.interviewdemo.ui.adapter

import com.android.interviewdemo.model.models.User

interface RecyclerItemListener {
    fun onItemClicked(item: User)
}