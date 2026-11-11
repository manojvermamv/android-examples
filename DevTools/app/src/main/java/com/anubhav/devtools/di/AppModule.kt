package com.anubhav.devtools.di

import com.anubhav.devtools.viewmodel.MainActivityViewModel
import com.anubhav.devtools.model.repository.MainRepository
import com.anubhav.devtools.model.local.ContentDataSource
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