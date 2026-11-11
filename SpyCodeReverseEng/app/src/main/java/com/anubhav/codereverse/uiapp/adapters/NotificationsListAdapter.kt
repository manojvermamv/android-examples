package com.anubhav.codereverse.uiapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.codereverse.databinding.ItemNotificationListBinding
import com.anubhav.codereverse.uiapp.database.retrofit.entity.NotificationListData
import com.anubhav.codereverse.uiapp.ui.common.afterMeasured
import com.anubhav.codereverse.uiapp.ui.common.setOnSingleClickListener


class NotificationsListAdapter internal constructor(private val context: Context, private val listener: Listener) :
    ListAdapter<NotificationListData, NotificationsListAdapter.ViewHolder>(NotificationListDiffCallback()) {

    class ViewHolder(private val binding: ItemNotificationListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotificationListData) {
            binding.displayTitle.text = item.title
            binding.expandTextView.text = item.message
            binding.datetime.text = item.dateTime
            //binding.expandTextView.afterMeasured { performClick() }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnSingleClickListener {
            listener.onItemClick(getItem(position))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemNotificationListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    interface Listener {
        fun onItemClick(data: NotificationListData)
        fun onItemLongClick(data: NotificationListData)
    }

}

class NotificationListDiffCallback : DiffUtil.ItemCallback<NotificationListData>() {
    override fun areItemsTheSame(oldItem: NotificationListData, newItem: NotificationListData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NotificationListData, newItem: NotificationListData): Boolean {
        return oldItem.dateTime == newItem.dateTime
    }
}