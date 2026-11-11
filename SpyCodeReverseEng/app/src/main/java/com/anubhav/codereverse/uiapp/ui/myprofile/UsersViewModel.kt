package com.anubhav.codereverse.uiapp.ui.myprofile

import android.content.Context
import android.net.Uri
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.anubhav.codereverse.uiapp.common.Result
import com.anubhav.codereverse.uiapp.database.firebase.entity.*
import com.anubhav.codereverse.uiapp.database.firebase.remote.FirebaseReferenceValueObserver
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseAuthRepository
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseDatabaseRepository
import com.anubhav.codereverse.uiapp.ui.common.BaseFragment
import com.anubhav.codereverse.uiapp.ui.common.BaseViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException

class UsersViewModel(
    authRepo: FirebaseAuthRepository,
    private val dbRepository: FirebaseDatabaseRepository
) : BaseViewModel(authRepo) {

    /**
     * User Observer
     * */
    private val firebaseReferenceObserver = FirebaseReferenceValueObserver()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _otherUser = MutableLiveData<User>()
    val otherUser: LiveData<User> = _otherUser

    private val _usersList = MutableLiveData<MutableList<User>>()
    val usersList = MediatorLiveData<List<User>>()
    val usersInfoList = MediatorLiveData<List<UserInfo>>()
    val usersImageList = MediatorLiveData<List<UserImage>>()

    init {
        usersList.addSource(_usersList) { list ->
            list.filter { it.info.uid != currentUid }
            usersList.value = list
        }

        usersInfoList.addSource(_usersList) { list ->
            val newList = mutableListOf<UserInfo>()
            list.forEach {
                if (it.info.uid != currentUid) newList.add(it.info)
            }
            //newList.shuffle()
            newList.sortBy { it.onlineStatus.not() }
            usersInfoList.value = newList
        }

        usersImageList.addSource(_usersList) { list ->
            val newList = mutableListOf<UserImage>()
            val mUser = list.find { it.info.uid == currentUid }
            mUser?.let { it ->
                it.images.asIterable().forEach { newList.add(it.value) }
            }
            usersImageList.value = newList
        }

        loadUser()
        loadUsers()
    }

    private fun loadUser() {
        dbRepository.loadAndObserveUser(currentUid, firebaseReferenceObserver) { result: Result<User> ->
            onResult(_user, result)
        }
    }

    fun loadUser(otherUserID: String) {
        dbRepository.loadAndObserveUser(otherUserID, firebaseReferenceObserver) { result: Result<User> ->
            onResult(_otherUser, result)
        }
    }

    private fun loadUsers() {
        dbRepository.loadAndObserveUsers(firebaseReferenceObserver) { result ->
            onResult(_usersList, result)
        }
    }

    /**
     * Update User Profile
     * */
    fun updateUserProfile(currentUser: UserInfo, newUser: UserInfo): Task<Unit> {
        return dbRepository.updateUserProfile(currentUser, newUser)
    }

    fun uploadUserAvatar(localImage: Uri, b: (() -> Unit)? = null) {
        dbRepository.uploadUserAvatar(localImage) { result ->
            onResult(null, result)
            if (result is Result.Success) b?.invoke()
        }
    }

    fun uploadUserImage(localImage: Uri, b: (() -> Unit)? = null) {
        dbRepository.uploadUserImage(localImage) { result ->
            onResult(null, result)
            if (result is Result.Success) b?.invoke()
        }
    }

    fun deleteUserImage(imageUrl: String, b: (() -> Unit)? = null) {
        dbRepository.deleteUserImage(imageUrl) { result ->
            onResult(null, result)
            if (result is Result.Success) b?.invoke()
        }
    }

    fun updateEmail(currentEmail: String, newEmail: String, b: ((Boolean) -> Unit)? = null) {
        if (currentEmail != newEmail) {
            authRepository.updateEmail(firebaseUser!!, newEmail) { result ->
                onResult(null, result)
                b?.invoke(result is Result.Success)
            }
        } else b?.invoke(false)
    }

    fun updatePassword(context: Context, currentPass: String, newPass: String, b: ((Boolean) -> Unit)? = null) {
        if (currentPass != newPass) {
            authRepository.updatePassword(firebaseUser!!, newPass) { result ->
                onResult(null, result)
                if (result is Result.Success) {
                    b?.invoke(true)
                } else if (result is Result.Error) {
                    val exception = result.exception!!
                    if (exception is FirebaseAuthRecentLoginRequiredException) {
                        authRepository.reAuthenticateUser(context) {
                            if (it is Result.Success) {
                                updatePassword(context, currentPass, newPass, b)
                            }
                        }
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        b?.invoke(false)
                    }
                }
            }
        } else b?.invoke(false)
    }

    fun checkUsernameExits(username: String, b: ((Boolean) -> Unit)? = null) {
        authRepository.checkUsernameExits(username) { result ->
            onResult(null, result)
            b?.invoke(result is Result.Success && result.data == true)
        }
    }

    fun uploadFCMToken(userID: String? = null, token: String) {
        var id = userID
        if (id.isNullOrEmpty()) id = currentUid
        if (token.isNotEmpty()) dbRepository.uploadFcmToken(id, token)
    }

    fun loadFCMToken(userID: String? = null, b: ((String) -> Unit)) {
        var id = userID
        if (id.isNullOrEmpty()) id = currentUid
        dbRepository.getFcmToken(id) { result ->
            onResult(null, result)
            if (result is Result.Success) {
                b.invoke(result.data ?: "")
            } else {
                b.invoke("")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseReferenceObserver.clear()
    }

    /**
     * Shared livedata functions
     * */

    val onSharedFragmentChangeClick = MutableLiveData<BaseFragment<out ViewDataBinding>>()
    val onSharedProgressBar = MutableLiveData<Boolean>()

    fun sharedFragmentChangeClick(clazz: BaseFragment<out ViewDataBinding>) {
        onSharedFragmentChangeClick.value = clazz
    }

    fun sharedProgressBar(enable: Boolean) {
        onSharedProgressBar.value = enable
    }

}