package com.android.interviewdemo.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.interviewdemo.databinding.FragmentHomeBinding
import com.android.interviewdemo.ui.adapter.CurrentOffersAdapter
import com.android.interviewdemo.ui.adapter.HorizontalSpacingItemDecoration
import com.android.interviewdemo.ui.adapter.LoadingViewAdapter
import com.android.interviewdemo.ui.adapter.TournamentsRvAdapter
import com.android.interviewdemo.ui.adapter.UpcomingMatchesRvAdapter
import com.android.interviewdemo.ui.adapter.UserMatchesAdapter
import com.android.interviewdemo.ui.common.BaseFragment
import com.android.interviewdemo.ui.common.GlobalAction
import com.android.interviewdemo.viewmodel.MainActivityViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel by activityViewModel<MainActivityViewModel>()

    private val tournamentsAdapter by lazy { TournamentsRvAdapter(binding.rvTournaments) }

    private val loadingViewAdapter by lazy { LoadingViewAdapter() }
    private val upcomingMatchesAdapter by lazy {
        UpcomingMatchesRvAdapter { position, _ ->
            viewModel.globalAction(GlobalAction.RecyclerItemClick(position))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTournamentsRecyclerView()
        setupUpcomingMatchesRecyclerView()

        fetchUserData()
        fetchTournaments()
        fetchUpcomingMatches()
    }

    private fun setupTournamentsRecyclerView() {
        binding.rvTournaments.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.rvTournaments.adapter = tournamentsAdapter
        binding.rvTournaments.addOnEndScrollListener {
            viewModel.featuredTournamentsRes.getData()?.nextPage?.let { page ->
                viewModel.featuredTournamentsRes.updatePageIndex(page)
            }
        }

        val itemSpacing = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._10sdp)
        binding.rvTournaments.addItemDecoration(HorizontalSpacingItemDecoration(itemSpacing))
    }

    private fun setupUpcomingMatchesRecyclerView() {
        binding.rvUpcomingMatches.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingMatches.adapter = ConcatAdapter(upcomingMatchesAdapter, loadingViewAdapter)
        binding.rvUpcomingMatches.addOnEndScrollListener {
            viewModel.upcomingMatchesRes.getData()?.nextPage?.let { page ->
                viewModel.upcomingMatchesRes.updatePageIndex(page)
            }
        }
    }

    private fun fetchUserData() {
        viewModel.userDataRes.getOrObserve(viewLifecycleOwner,
            onLoading = {
                viewModel.triggerProgressBar(it)
                binding.tvUserMatches.isInvisible = it
                binding.layUserMatches.isInvisible = it
                binding.offersSlider.isInvisible = it
            },
            onSuccess = {
                it.walletSummary?.let { wallet -> viewModel.updateWallet(wallet) }
                it.userMatches?.matchList?.let { matches ->
                    binding.userMatchesSlider.adapter = UserMatchesAdapter(requireContext(), matches)
                }
                it.currentOffers?.offerList?.let { offers ->
                    repeat (1) { offers.addAll(offers) }
                    binding.offersSlider.adapter = CurrentOffersAdapter(requireContext(), offers)
                }
            },
            onError = {
                showError(it) { viewModel.userDataRes.refresh() }
            }
        )
    }

    private fun fetchTournaments() {
        viewModel.featuredTournamentsRes.getOrObserve(viewLifecycleOwner,
            onLoading = {
                if (viewModel.featuredTournamentsRes.getCurrentPage() == 1) {
                    binding.rvTournaments.isInvisible = it
                    binding.tvTournaments.isInvisible = it
                }
            },
            onSuccess = {
                val tournaments = viewModel.featuredTournamentsRes.appendList(it.matchList)
                tournamentsAdapter.updateList(tournaments)
            }
        )
    }

    private fun fetchUpcomingMatches() {
        viewModel.upcomingMatchesRes.getOrObserve(viewLifecycleOwner,
            onLoading = {
                if (viewModel.upcomingMatchesRes.getCurrentPage() == 1) {
                    binding.rvUpcomingMatches.isInvisible = it
                    binding.tvUpcomingMatches.isInvisible = it
                } else {
                    loadingViewAdapter.setLoading(it)
                }
            },
            onSuccess = {
                val matches = viewModel.upcomingMatchesRes.appendList(it.matchList)
                upcomingMatchesAdapter.updateList(matches)
            }
        )
    }

    private fun showError(message: String? = null, refreshData: () -> Unit) {
        Snackbar.make(binding.root, message ?: "Some Error Occurred", Snackbar.LENGTH_LONG)
            .setAction("Retry") { refreshData() }
            .show()
    }

}