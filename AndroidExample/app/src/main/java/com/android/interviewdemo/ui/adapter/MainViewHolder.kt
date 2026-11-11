package com.android.interviewdemo.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.android.interviewdemo.R
import com.android.interviewdemo.databinding.ListItemBinding
import com.android.interviewdemo.model.models.User
import com.squareup.picasso.Picasso

class MainViewHolder(private val itemBinding: ListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(item: User, recyclerItemListener: RecyclerItemListener) {
        itemBinding.tvTitle.text = item.getFullName()
        itemBinding.tvTotalPrice.text = "${item.getPrice()} rs"
        Picasso.get().load(item.getImageUrl()).placeholder(R.drawable.img_not_available).error(R.drawable.img_not_available).into(itemBinding.ivProfile)
        itemBinding.main.setOnClickListener { recyclerItemListener.onItemClicked(item) }
    }
}