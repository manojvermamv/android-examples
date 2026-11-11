package com.android.interviewdemo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.interviewdemo.model.User
import com.android.interviewdemo.repository.MainActivityRepository

class MainActivityViewModel : ViewModel() {

    var totalPrice: MutableLiveData<Int> = MutableLiveData(0)

    fun getUsers(): LiveData<List<User>> {
        return MainActivityRepository.getUsersApiCall()
    }

    fun setPrice(price: Int) {
        totalPrice.value = price
    }

    fun updatePrice(price: Int, isIncrement: Boolean) {
        val lastPrice = totalPrice.value ?: 0
        totalPrice.value = if (isIncrement) (lastPrice + price) else (lastPrice - price)
    }

}