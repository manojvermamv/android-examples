package com.android.interviewdemo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.interviewdemo.model.models.User
import com.android.interviewdemo.utils.SingleEvent
import com.android.interviewdemo.viewmodel.repository.MainActivityRepository

class MainActivityViewModel : ViewModel() {

    private val totalPricePrivate = MutableLiveData<Int>()
    val totalPrice: LiveData<Int> get() = totalPricePrivate

    fun setPrice(price: Int) {
        totalPricePrivate.value = price
    }

    fun updatePrice(price: Int, isIncrement: Boolean) {
        val lastPrice = totalPricePrivate.value ?: 0
        totalPricePrivate.value = if (isIncrement) (lastPrice + price) else (lastPrice - price)
    }

    /**
     * UI actions as event, user action is single one time event, Shouldn't be multiple time consumption
     */
    private val openDetailsScreenPrivate = MutableLiveData<SingleEvent<User>>()
    val openDetailsScreen: LiveData<SingleEvent<User>> get() = openDetailsScreenPrivate

    fun openDetailsScreen(item: User) {
        openDetailsScreenPrivate.value = SingleEvent(item)
    }

    /**
     * Fetch data from api request
     * */
    fun getUsers(): LiveData<List<User>> = MainActivityRepository.getUsersApiCall()

}