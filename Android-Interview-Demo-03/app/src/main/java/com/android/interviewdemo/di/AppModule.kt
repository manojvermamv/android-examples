package com.android.interviewdemo.di

import com.android.interviewdemo.model.local.ContentDataSource
import com.android.interviewdemo.model.repository.MainRepository
import com.android.interviewdemo.viewmodel.MainActivityViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {
    single { ContentDataSource(androidContext().contentResolver) }
    single { MainRepository(get()) }
    viewModel {
        MainActivityViewModel(androidApplication(), get())
    }
}