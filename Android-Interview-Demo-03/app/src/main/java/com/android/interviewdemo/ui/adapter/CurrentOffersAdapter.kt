package com.android.interviewdemo.ui.adapter

import android.content.Context
import android.view.ViewGroup
import com.android.interviewdemo.databinding.SliderCurrentOffersBinding
import com.android.interviewdemo.model.models.OfferList
import com.android.interviewdemo.ui.common.BaseViewHolder
import com.android.interviewdemo.utils.loadImage
import com.github.islamkhsh.CardSliderAdapter

class CurrentOffersAdapter constructor(
    private val context: Context,
    private var itemList: ArrayList<OfferList>,
    private val onItemClicked: (OfferList) -> Unit = {}
) : CardSliderAdapter<BaseViewHolder<SliderCurrentOffersBinding>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BaseViewHolder.create(parent, SliderCurrentOffersBinding::inflate)

    override fun bindVH(holder: BaseViewHolder<SliderCurrentOffersBinding>, position: Int) {
        val data = itemList[position]
        holder.binding.ivImage.loadImage(data.offerBannerUrl ?: "")
        holder.itemView.setOnClickListener { onItemClicked(itemList[position]) }
        holder.itemView.tag = position
    }

    override fun getItemCount() = itemList.size

    fun updateList(list: ArrayList<OfferList>) {
        itemList = list
        notifyDataSetChanged()
    }

    fun updateItemAt(data: OfferList) {
        itemList.find { it.type == data.type }?.also {
            val index = itemList.indexOf(it)
            itemList[index] = data
            notifyItemChanged(index, data)
        }
    }

}