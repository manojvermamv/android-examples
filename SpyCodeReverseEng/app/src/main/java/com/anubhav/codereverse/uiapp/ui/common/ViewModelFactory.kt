package com.anubhav.codereverse.uiapp.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.anubhav.codereverse.uiapp.ui.home.HomeViewModel
import com.anubhav.codereverse.uiapp.ui.login.LoginViewModel
import com.anubhav.codereverse.uiapp.ui.myprofile.UsersViewModel
import com.anubhav.codereverse.uiapp.ui.notifications.NotificationsViewModel
import com.anubhav.codereverse.uiapp.ui.register.AuthViewModel
import com.anubhav.codereverse.uiapp.ui.register.RegisterViewModel

class ViewModelFactory(private val app: MyApp, private val otherUserID: String? = null) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepository = app.authRepository
        val dbRepository = app.databaseRepository

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository, dbRepository, app) as T
        } else if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authRepository, dbRepository, app) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(authRepository, dbRepository, app) as T
        } else if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(authRepository, dbRepository) as T
        } else if (modelClass.isAssignableFrom(UsersViewModel::class.java)) {
            return UsersViewModel(authRepository, dbRepository) as T
        } else if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            return NotificationsViewModel(authRepository, dbRepository) as T
        } else {
            error("Unknown view model class $modelClass")
        }
    }

}