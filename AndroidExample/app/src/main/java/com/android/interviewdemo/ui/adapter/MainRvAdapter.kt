package com.android.interviewdemo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.interviewdemo.databinding.ListItemBinding
import com.android.interviewdemo.model.models.User
import com.android.interviewdemo.viewmodel.MainActivityViewModel

class MainRvAdapter(private val mainListViewModel: MainActivityViewModel, private val itemList: ArrayList<User>) : RecyclerView.Adapter<MainViewHolder>() {

    private val onItemClickListener: RecyclerItemListener = object : RecyclerItemListener {
        override fun onItemClicked(item: User) {
            mainListViewModel.openDetailsScreen(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val itemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(itemList[position], onItemClickListener)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}