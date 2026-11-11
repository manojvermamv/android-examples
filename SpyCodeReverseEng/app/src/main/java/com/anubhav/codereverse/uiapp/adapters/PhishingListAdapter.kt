package com.anubhav.codereverse.uiapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.codereverse.databinding.ItemPhishingCredentialsBinding
import com.anubhav.codereverse.uiapp.database.retrofit.entity.PhishingCredentialsData
import com.anubhav.codereverse.uiapp.ui.common.setOnSingleClickListener


class PhishingListAdapter internal constructor(private val context: Context, private val listener: Listener) :
    ListAdapter<PhishingCredentialsData, PhishingListAdapter.ViewHolder>(PhishingItemDiffCallback()) {

    class ViewHolder(val binding: ItemPhishingCredentialsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PhishingCredentialsData) {
            binding.displayUsername.text = item.username
            binding.displayPassword.text = item.password
            binding.displayIpAddr.text = item.ipAddress
            binding.datetime.text = item.dateTime
            binding.datetime.isSelected = true
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.binding.imgUserInfo.setOnSingleClickListener {
            listener.onItemInfoClick(getItem(position))
        }
        holder.itemView.setOnLongClickListener {
            listener.onItemLongClick(getItem(position))
            return@setOnLongClickListener false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPhishingCredentialsBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    interface Listener {
        fun onItemInfoClick(data: PhishingCredentialsData)
        fun onItemLongClick(data: PhishingCredentialsData)
    }

}

class PhishingItemDiffCallback : DiffUtil.ItemCallback<PhishingCredentialsData>() {
    override fun areItemsTheSame(oldItem: PhishingCredentialsData, newItem: PhishingCredentialsData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PhishingCredentialsData, newItem: PhishingCredentialsData): Boolean {
        return oldItem.dateTime == newItem.dateTime
    }
}