package com.android.interviewdemo.ui.common

import com.android.interviewdemo.model.models.WalletSummary

sealed class GlobalAction<out T : Any> {
    data object MenuClick : GlobalAction<Unit>()
    data object WalletClick : GlobalAction<Unit>()
    data object NotificationClick : GlobalAction<Unit>()
    data class RecyclerItemClick(val position: Int) : GlobalAction<Int>()
}

sealed class UiAction<out T : Any> {
    data object Ideal : UiAction<Nothing>()
    data class UpdateWallet(val data: WalletSummary) : UiAction<WalletSummary>()
}