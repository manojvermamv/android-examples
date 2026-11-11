package com.anubhav.devtools.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anubhav.devtools.R
import com.anubhav.devtools.databinding.ToolItemBinding
import com.anubhav.devtools.model.models.Tool
import com.squareup.picasso.Picasso

class ToolRvAdapter(
    private val itemList: ArrayList<Tool>,
    private val onClick: (index: Int, tool: Tool) -> Unit
) : RecyclerView.Adapter<ToolRvAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ToolItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Tool, onClick: (index: Int, tool: Tool) -> Unit) {
            binding.tvTitle.text = item.title()
            binding.tvDesc.text = item.description
            if (item.imageRes != 0) {
                Picasso.get().load(item.imageRes).error(R.drawable.img_not_available).into(binding.ivProfile)
            }

            binding.rootView.setOnClickListener {
                val currentIndex = it.tag.toString().toInt()
                onClick(currentIndex, item)
            }
            binding.rootView.setOnLongClickListener {
                val currentIndex = it.tag.toString().toInt()
                onClick(currentIndex, item)
                return@setOnLongClickListener true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ToolItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position], onClick)
        holder.itemView.tag = position
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}