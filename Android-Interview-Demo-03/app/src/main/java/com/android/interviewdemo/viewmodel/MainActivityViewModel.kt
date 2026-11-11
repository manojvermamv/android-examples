package com.android.interviewdemo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.interviewdemo.model.models.Match
import com.android.interviewdemo.model.models.TournamentList
import com.android.interviewdemo.model.models.UpcomingMatches
import com.android.interviewdemo.model.models.WalletSummary
import com.android.interviewdemo.model.repository.MainRepository
import com.android.interviewdemo.ui.common.GlobalAction
import com.android.interviewdemo.ui.common.UiAction
import com.android.interviewdemo.utils.ApiDataLoader
import com.android.interviewdemo.utils.ApiDataLoaderPaged
import com.android.interviewdemo.utils.SingleEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(
    context: Application,
    private val mainRepository: MainRepository,
) : AndroidViewModel(context) {

    val userDataRes = ApiDataLoader { mainRepository.getUserDataApiCall() }

    val featuredTournamentsRes = ApiDataLoaderPaged<Match, TournamentList> { page ->
        mainRepository.getFeaturedTournamentsApiCall(page)
    }

    val upcomingMatchesRes = ApiDataLoaderPaged<Match, UpcomingMatches> { page ->
        mainRepository.getUpcomingMatchesApiCall(page)
    }

    /**
     * UI actions as event, user action is single one time event, Shouldn't be multiple time consumption
     */

    private val _progressBar = MutableLiveData<SingleEvent<Boolean>>()
    val progressBar: LiveData<SingleEvent<Boolean>> get() = _progressBar

    fun triggerProgressBar(show: Boolean) {
        _progressBar.value = SingleEvent(show)
    }

    /**
     * SharedFlow: Use for one-time events like navigation commands, toast messages, or any actions where you're interested in broadcasting events but not holding state
     * */
    private val _globalActions = MutableSharedFlow<GlobalAction<Any>>()
    val globalAction: SharedFlow<GlobalAction<Any>> = _globalActions.asSharedFlow()

    fun globalAction(action: GlobalAction<Any>) {
        viewModelScope.launch {
            _globalActions.emit(action)
        }
    }

    /**
     * StateFlow: Use when you need to hold and expose the latest state (e.g., UI state, form data)
     * */
    private val _uiActions = MutableStateFlow<UiAction<Any>>(UiAction.Ideal)
    val uiAction: StateFlow<UiAction<Any>> = _uiActions.asStateFlow()

    fun uiAction(action: UiAction<Any>) {
        viewModelScope.launch {
            _uiActions.value = action
        }
    }

    fun updateWallet(walletSummary: WalletSummary) {
        viewModelScope.launch {
            _uiActions.value = UiAction.UpdateWallet(walletSummary)
        }
    }

}