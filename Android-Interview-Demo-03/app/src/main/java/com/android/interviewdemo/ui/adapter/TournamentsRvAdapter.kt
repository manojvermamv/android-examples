package com.android.interviewdemo.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.interviewdemo.databinding.ItemFeaturedTournamentsBinding
import com.android.interviewdemo.model.models.Match
import com.android.interviewdemo.ui.adapter.TournamentsRvAdapter.ViewHolder
import com.android.interviewdemo.utils.formatDateTime
import com.android.interviewdemo.utils.formatTimeRemaining
import com.android.interviewdemo.utils.loadImage
import com.android.interviewdemo.utils.loadLogo


class TournamentsRvAdapter(
    private val recyclerView: RecyclerView,
    private val onItemClicked: (item: Match) -> Unit = {}
) : ListAdapter<Match, ViewHolder>(MatchDiffUtilCallback()) {

    class ViewHolder(val binding: ItemFeaturedTournamentsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeaturedTournamentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // Get RecyclerView's width and calculate item width (e.g., 86% of the screen width)
        binding.root.apply {
            val mLayoutParams = layoutParams
            val itemWidth = (recyclerView.width * 0.86).toInt()
            mLayoutParams.width = itemWidth
            layoutParams = mLayoutParams
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = getItem(position)
            tvMatchFormat.text = item.matchFormat
            tvTournamentName.text = item.tournamentName
            layLineupsOut.isVisible = item.metadata?.isLineupOut ?: false

            val duration = (item.startsAt ?: "0").toLong()
            tvTimer.text = formatTimeRemaining(duration)
            tvDate.text = formatDateTime(duration)

            item.teams?.a?.let {
                tvTeamACode.text = it.code
                ivTeamALogo.loadLogo(it)
                bgTeamALogo.isVisible = it.logoBgColor != null
                it.logoBgColor?.let { color ->
                    bgTeamALogo.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
                }
            }
            item.teams?.b?.let {
                tvTeamBCode.text = it.code
                ivTeamBLogo.loadLogo(it)
                bgTeamBLogo.isVisible = it.logoBgColor != null
                it.logoBgColor?.let { color ->
                    bgTeamBLogo.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
                }
            }

            //layOffer.isVisible = item.matchOffers.isNotEmpty()
            item.matchOffers.getOrNull(0)?.let {
                tvOfferTitle.text = it.title
                tvOfferSubText.text = it.subtext
                ivOfferIcon.loadImage(it.offerIconUrl)
            }
        }

        holder.itemView.setOnClickListener { onItemClicked(getItem(position)) }
    }

    fun getItemAt(position: Int): Match {
        return getItem(position)
    }

    fun updateList(list: ArrayList<Match>) {
        submitList(list.toMutableList())
    }

    fun appendList(list: ArrayList<Match>) {
        val newList = currentList.toMutableList()
        newList.addAll(list)
        submitList(newList)
    }

}