package com.android.interviewdemo.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.android.interviewdemo.model.models.Match

class MatchDiffUtilCallback : DiffUtil.ItemCallback<Match>() {
    override fun areItemsTheSame(oldItem: Match, newItem: Match): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Match, newItem: Match): Boolean {
        return oldItem.id == newItem.id
    }
}