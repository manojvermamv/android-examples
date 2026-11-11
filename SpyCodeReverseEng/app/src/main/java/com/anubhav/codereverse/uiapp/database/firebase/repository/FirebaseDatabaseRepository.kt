package com.anubhav.codereverse.uiapp.database.firebase.repository

import android.net.Uri
import com.anubhav.codereverse.uiapp.firebase.common.addOnFailure
import com.anubhav.codereverse.uiapp.firebase.common.toUnit
import com.anubhav.codereverse.uiapp.common.IConstants.REF_USER_INFO
import com.anubhav.codereverse.uiapp.common.IConstants.REF_USER_UID
import com.anubhav.codereverse.uiapp.database.firebase.entity.*
import com.anubhav.codereverse.uiapp.common.Result
import com.anubhav.codereverse.uiapp.data.model.DefaultConfig
import com.anubhav.codereverse.uiapp.database.firebase.remote.*
import com.anubhav.codereverse.uiapp.firebase.common.wrapSnapshotToArrayList
import com.anubhav.codereverse.uiapp.firebase.common.wrapSnapshotToClass
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth


class FirebaseDatabaseRepository {

    private val firebaseDatabaseService = FirebaseDataSource()
    private val firebaseStorageService = FirebaseStorageSource()

    fun currentUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    //region Update Profile
    fun updateUserProfile(currentUser: UserInfo, newUser: UserInfo): Task<Unit> {
        return firebaseDatabaseService.updateUserProfile(currentUser.uid, userInfoChanges(newUser, currentUser)).toUnit()
    }

    fun uploadUserAvatar(localImage: Uri, b: (Result<Unit>) -> Unit) {
        b.invoke(Result.Loading)
        firebaseStorageService.uploadUserAvatar(currentUid()!!, localImage) { uri, exception ->
            if (uri != null) {
                val updatesMap = mutableMapOf<String, Any?>()
                updatesMap["profileUrl"] = uri.toString()
                firebaseDatabaseService.updateUserProfile(currentUid()!!, updatesMap).addOnSuccessListener {
                    b.invoke(Result.Success(msg = "User avatar uploaded successfully"))
                }.addOnFailureListener { b.invoke(Result.Error(exception = it, msg = it.message)) }
            } else {
                b.invoke(Result.Error(exception = exception, msg = exception?.message))
            }
        }
    }

    fun uploadUserImage(localImage: Uri, b: (Result<Unit>) -> Unit) {
        b.invoke(Result.Loading)
        firebaseStorageService.uploadUserImage(currentUid()!!, localImage) { uri, exception ->
            if (uri != null) {
                val image = uri.toString()
                firebaseDatabaseService.updateUserImage(currentUid()!!, UserImage(image)).addOnSuccessListener {
                    b.invoke(Result.Success(msg = "Uploaded successfully"))
                }.addOnFailureListener { b.invoke(Result.Error(exception = it, msg = it.message)) }
            } else {
                b.invoke(Result.Error(exception = exception, msg = exception?.message))
            }
        }
    }

    fun uploadChatImage(localImage: Uri, fileName:String, b: (Result<Uri>) -> Unit) {
        firebaseStorageService.uploadChatImage(currentUid()!!, localImage, fileName, b)
    }

    fun uploadChatAttachment(localImage: Uri, fileName:String, b: (Result<Uri>) -> Unit) {
        firebaseStorageService.uploadChatAttachment(currentUid()!!, localImage, fileName, b)
    }

    fun deleteUserImage(imageUrl: String, b: (Result<Unit>) -> Unit) {
        b.invoke(Result.Loading)
        firebaseStorageService.deleteUserImage(imageUrl) {
            firebaseDatabaseService.removeUserImage(currentUid()!!, UserImage(imageUrl)) {
                b.invoke(Result.Success(msg = "Deleted successfully"))
            }
        }
    }


    fun uploadFcmToken(userID: String, token: String) {
        firebaseDatabaseService.uploadFcmToken(userID, token)
    }
    //endregion

    //region Update
    fun updateNewUser(user: UserInfo, b: (() -> Unit)? = null) {
        firebaseDatabaseService.updateNewUser(user, b)
    }

    fun updateUserEmail(userID: String, email: String): Task<Unit> {
        return firebaseDatabaseService.updateUserEmail(userID, email)
    }

    fun updateUserPassword(userID: String, password: String): Task<Unit> {
        return firebaseDatabaseService.updateUserPassword(userID, password)
    }

    fun updateNewFriend(myUser: UserFriend, otherUser: UserFriend) {
        firebaseDatabaseService.updateNewFriend(myUser, otherUser)
    }

    fun updateNewSentRequest(userID: String, userRequest: UserRequest) {
        firebaseDatabaseService.updateNewSentRequest(userID, userRequest)
    }

    fun updateNewNotification(otherUserID: String, userNotification: UserNotification) {
        firebaseDatabaseService.updateNewNotification(otherUserID, userNotification)
    }

    //endregion

    //region Remove
    fun removeNotification(userID: String, notificationID: String) {
        firebaseDatabaseService.removeNotification(userID, notificationID)
    }

    fun removeFriend(userID: String, friendID: String) {
        firebaseDatabaseService.removeFriend(userID, friendID)
    }

    fun removeSentRequest(otherUserID: String, myUserID: String) {
        firebaseDatabaseService.removeSentRequest(otherUserID, myUserID)
    }

    //endregion

    //region Check
    fun getFcmToken(userID: String, b: ((Result<String>) -> Unit)) {
        firebaseDatabaseService.getFcmToken(userID).addOnSuccessListener { snap ->
            b.invoke(Result.Success(wrapSnapshotToClass(String::class.java, snap)))
        }.addOnFailure(b)
    }

    fun checkUserCreated(userID: String, b: ((Result<Unit>) -> Unit)) {
        firebaseDatabaseService.loadUserTask(userID).addOnSuccessListener { snap ->
            if (snap != null && snap.child("$REF_USER_INFO/$REF_USER_UID").exists()) {
                b.invoke(Result.Success())
            } else {
                b.invoke(Result.Error("User not exits in database"))
            }
        }.addOnFailure(b)
    }

    fun checkUsernameExits(username: String, b: ((Result<Boolean>) -> Unit)) {
        firebaseDatabaseService.checkUsernameTask(username).addOnSuccessListener { snap ->
            val usernameExits = snap.exists() && snap.childrenCount > 0
            b.invoke(Result.Success(usernameExits))
        }.addOnFailure(b)
    }

    //endregion

    //region Load Single

    fun loadUser(userID: String, b: ((Result<User>) -> Unit)) {
        firebaseDatabaseService.loadUserTask(userID).addOnSuccessListener {
            b.invoke(Result.Success(wrapSnapshotToClass(User::class.java, it)))
        }.addOnFailure(b)
    }

    fun loadUserInfo(userID: String, b: ((Result<UserInfo>) -> Unit)) {
        firebaseDatabaseService.loadUserInfoTask(userID).addOnSuccessListener {
            b.invoke(Result.Success(wrapSnapshotToClass(UserInfo::class.java, it)))
        }.addOnFailure(b)
    }

    fun loadConfig(b: ((Result<DefaultConfig>) -> Unit)) {
        b.invoke(Result.Loading)
        firebaseDatabaseService.loadConfig().addOnSuccessListener {
            val config = wrapSnapshotToClass(DefaultConfig::class.java, it) ?: DefaultConfig()
            b.invoke(Result.Success(config))
        }.addOnFailure(b)
    }

    //endregion

    //region Load List

    fun loadUsers(b: ((Result<MutableList<User>>) -> Unit)) {
        b.invoke(Result.Loading)
        firebaseDatabaseService.loadUsersTask().addOnSuccessListener {
            val usersList = wrapSnapshotToArrayList(User::class.java, it)
            b.invoke(Result.Success(usersList))
        }.addOnFailure(b)
    }

    fun loadFriends(userID: String, b: ((Result<List<UserFriend>>) -> Unit)) {
        b.invoke(Result.Loading)
        firebaseDatabaseService.loadFriendsTask(userID).addOnSuccessListener {
            val friendsList = wrapSnapshotToArrayList(UserFriend::class.java, it)
            b.invoke(Result.Success(friendsList))
        }.addOnFailure(b)
    }

    fun loadNotifications(userID: String, b: ((Result<MutableList<UserNotification>>) -> Unit)) {
        b.invoke(Result.Loading)
        firebaseDatabaseService.loadNotificationsTask(userID).addOnSuccessListener {
            val notificationsList = wrapSnapshotToArrayList(UserNotification::class.java, it)
            b.invoke(Result.Success(notificationsList))
        }.addOnFailure(b)
    }

    //endregion

    //#region Load and Observe

    fun loadAndObserveUser(userID: String, observer: FirebaseReferenceValueObserver, b: ((Result<User>) -> Unit)) {
        firebaseDatabaseService.attachUserObserver(User::class.java, userID, observer, b)
    }

    fun loadAndObserveUsers(observer: FirebaseReferenceValueObserver, b: ((Result<MutableList<User>>) -> Unit)) {
        firebaseDatabaseService.attachUsersObserver(User::class.java, observer, b)
    }

    fun loadAndObserveUserInfo(userID: String, observer: FirebaseReferenceValueObserver, b: ((Result<UserInfo>) -> Unit)) {
        firebaseDatabaseService.attachUserInfoObserver(UserInfo::class.java, userID, observer, b)
    }

    fun loadAndObserveUserNotifications(userID: String, observer: FirebaseReferenceValueObserver, b: ((Result<MutableList<UserNotification>>) -> Unit)){
        firebaseDatabaseService.attachUserNotificationsObserver(UserNotification::class.java, userID, observer, b)
    }

    fun loadAndObserveUserFriends(userID: String, observer: FirebaseReferenceValueObserver, b: ((Result<MutableList<UserFriend>>) -> Unit)){
        firebaseDatabaseService.attachUserFriendsObserver(UserFriend::class.java, userID, observer, b)
    }

    fun loadAndObserveConfig(observer: FirebaseReferenceValueObserver, b: ((Result<DefaultConfig>) -> Unit)) {
        firebaseDatabaseService.attachConfigObserver(DefaultConfig::class.java, observer, b)
    }
    //endregion
}