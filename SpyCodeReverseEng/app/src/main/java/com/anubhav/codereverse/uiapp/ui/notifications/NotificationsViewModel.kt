package com.anubhav.codereverse.uiapp.ui.notifications

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.anubhav.codereverse.uiapp.common.Result
import com.anubhav.codereverse.uiapp.data.common.addNewItem
import com.anubhav.codereverse.uiapp.data.common.removeItem
import com.anubhav.codereverse.uiapp.database.firebase.entity.*
import com.anubhav.codereverse.uiapp.database.firebase.remote.FirebaseReferenceValueObserver
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseAuthRepository
import com.anubhav.codereverse.uiapp.database.firebase.repository.FirebaseDatabaseRepository
import com.anubhav.codereverse.uiapp.ui.common.BaseFragment
import com.anubhav.codereverse.uiapp.ui.common.BaseViewModel

class NotificationsViewModel(
    authRepo: FirebaseAuthRepository,
    private val dbRepository: FirebaseDatabaseRepository
) : BaseViewModel(authRepo) {

    private var currentUserFriend = UserFriend(currentUid, false)
    private var receiverUserFriend = UserFriend("", false)
    private val firebaseReferenceObserver = FirebaseReferenceValueObserver()

    private val updatedUserInfo = MutableLiveData<UserInfo>()
    private val userNotificationsList = MutableLiveData<MutableList<UserNotification>>()
    val usersInfoList = MediatorLiveData<MutableList<UserInfo>>().apply { value = mutableListOf() }

    init {
        usersInfoList.addSource(updatedUserInfo) { usersInfoList.addNewItem(it) }
        loadNotifications()
    }

    override fun onCleared() {
        super.onCleared()
        firebaseReferenceObserver.clear()
    }

    private fun loadNotifications() {
        onResult(null, Result.Loading)
        dbRepository.loadNotifications(currentUid) { result: Result<MutableList<UserNotification>> ->
            onResult(userNotificationsList, result)
            if (result is Result.Success) result.data?.forEach { loadUserInfo(it) }
        }
    }

    private fun loadUserInfo(userNotification: UserNotification) {
        dbRepository.loadUserInfo(userNotification.userID) { result: Result<UserInfo> ->
            onResult(updatedUserInfo, result)
        }
    }

    private fun updateNotification(otherUserInfo: UserInfo) {
        val userNotification = userNotificationsList.value?.find {
            it.userID == otherUserInfo.uid
        }

        if (userNotification != null) {
            dbRepository.updateNewFriend(currentUserFriend, receiverUserFriend)
            dbRepository.removeNotification(currentUid, otherUserInfo.uid)
            dbRepository.removeSentRequest(otherUserInfo.uid, currentUid)

            usersInfoList.removeItem(otherUserInfo)
            userNotificationsList.removeItem(userNotification)
        }
    }

    fun acceptNotificationPressed(userInfo: UserInfo) {
        currentUserFriend = UserFriend(currentUid, true)
        receiverUserFriend = UserFriend(userInfo.uid, true)
        updateNotification(userInfo)
    }

    fun declineNotificationPressed(userInfo: UserInfo) {
        currentUserFriend = UserFriend(currentUid, false)
        receiverUserFriend = UserFriend(userInfo.uid, false)
        updateNotification(userInfo)
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