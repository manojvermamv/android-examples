package com.anubhav.codereverse.uiapp.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.codereverse.databinding.ItemMasksingUrlBinding
import com.anubhav.codereverse.uiapp.data.model.MaskingUrlData
import com.anubhav.codereverse.uiapp.ui.common.setOnSingleClickListener
import com.anubhav.codereverse.uiapp.ui.common.showToast


class MaskingUrlAdapter internal constructor(private val context: Context, val listener: Listener) :
    ListAdapter<MaskingUrlData, MaskingUrlAdapter.ViewHolder>(MaskingUrlDiffCallback()) {

    class ViewHolder(val binding: ItemMasksingUrlBinding, val listener: Listener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, item: MaskingUrlData) {
            binding.expandTextView.text = item.originalUrl
            binding.maskingLink.text = item.maskingUrl
            binding.maskingLink.isSelected = true
            binding.datetime.text = item.dateTime
            binding.datetime.isSelected = true

            binding.btnCopyUrl.setOnSingleClickListener {
                val clipboard: ClipboardManager = context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Sharing link", item.maskingUrl)
                clipboard.setPrimaryClip(clip)
                context.showToast("Masking link copied.")
            }
            itemView.isFocusable = true
            itemView.setOnLongClickListener {
                listener.onItemLongClick(item)
                return@setOnLongClickListener false
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemMasksingUrlBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, listener)
    }

    fun itemExits(maskingUrl: String): Boolean {
        for (i in 0 until itemCount) {
            if (getItem(i).maskingUrl == maskingUrl) {
                return true
            }
        }
        return false
    }

    fun getItemAt(position: Int): MaskingUrlData {
        return getItem(position)
    }

    interface Listener {
        fun onItemLongClick(data: MaskingUrlData)
    }

}

class MaskingUrlDiffCallback : DiffUtil.ItemCallback<MaskingUrlData>() {
    override fun areItemsTheSame(oldItem: MaskingUrlData, newItem: MaskingUrlData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MaskingUrlData, newItem: MaskingUrlData): Boolean {
        return oldItem.dateTime == newItem.dateTime
    }
}