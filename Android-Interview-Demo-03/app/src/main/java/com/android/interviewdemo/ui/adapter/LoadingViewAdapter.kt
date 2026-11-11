package com.android.interviewdemo.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android.interviewdemo.databinding.ItemLoadingBinding
import com.android.interviewdemo.ui.common.BaseViewHolder
import com.android.interviewdemo.utils.afterMeasured


class LoadingViewAdapter constructor(private val onLoadingStarted: (() -> Unit)? = null) :
    RecyclerView.Adapter<BaseViewHolder<ItemLoadingBinding>>() {

    companion object {
        const val VIEW_TYPE_IDLE = 1
        const val VIEW_TYPE_LOADING = 2
    }

    private var items = listOf(VIEW_TYPE_IDLE)
    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BaseViewHolder.create(parent, ItemLoadingBinding::inflate)

    override fun onBindViewHolder(holder: BaseViewHolder<ItemLoadingBinding>, position: Int) {
        if (position == RecyclerView.NO_POSITION) return

        holder.binding.progress.isVisible = false
        when (items[position]) {
            VIEW_TYPE_LOADING -> {
                holder.binding.progress.isVisible = true
                holder.binding.rootView.afterMeasured {
                    flingRecyclerView()
                    //postDelayed({ flingRecyclerView() }, 100)
                }
            }
            else -> {
                println("LoadingViewAdapter Idle Position")
            }
        }
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setLoading(loading: Boolean) {
        this.items = listOf(
            if (loading)
                VIEW_TYPE_LOADING
            else
                VIEW_TYPE_IDLE
        )
        this.notifyDataSetChanged()
    }

    private fun flingRecyclerView() {
        if (onLoadingStarted != null) {
            onLoadingStarted.invoke()
        } else {
            recyclerView?.fling(0, 2000)
        }
    }

}