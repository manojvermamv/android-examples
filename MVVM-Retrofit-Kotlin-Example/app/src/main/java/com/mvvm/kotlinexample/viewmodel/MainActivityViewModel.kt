package com.mvvm.kotlinexample.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mvvm.kotlinexample.model.ServicesSetterGetter
import com.mvvm.kotlinexample.repository.MainActivityRepository

class MainActivityViewModel : ViewModel() {

    var servicesLiveData: MutableLiveData<ServicesSetterGetter>? = null

    fun getUser(): LiveData<ServicesSetterGetter>? {
        servicesLiveData = MainActivityRepository.getServicesApiCall()
        return servicesLiveData
    }

}