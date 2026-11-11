package com.anubhav.codereverse.uiapp.database.firebase.remote

import com.anubhav.codereverse.uiapp.firebase.common.toUnit
import com.anubhav.codereverse.uiapp.common.IConstants.*
import com.anubhav.codereverse.uiapp.database.firebase.entity.*
import com.anubhav.codereverse.uiapp.common.Result
import com.anubhav.codereverse.uiapp.firebase.common.database
import com.anubhav.codereverse.uiapp.firebase.common.wrapSnapshotToArrayList
import com.anubhav.codereverse.uiapp.firebase.common.wrapSnapshotToClass
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.*

class FirebaseReferenceConnectedObserver {


    private var valueEventListener: ValueEventListener? = null
    private var dbRef: DatabaseReference? = null
    private var userRef: DatabaseReference? = null

    fun start(userID: String) {
        this.userRef = database.child("${REF_USERS_ROOT}/$userID/$REF_USER_INFO/$REF_USER_ONLINE")
        this.valueEventListener = getEventListener()
        this.dbRef = FirebaseDatabase.getInstance().getReference(".info/connected")
            .apply { addValueEventListener(valueEventListener!!) }
    }

    private fun getEventListener(): ValueEventListener {
        return (object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    userRef?.setValue(true)
                    userRef?.onDisconnect()?.setValue(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun clear() {
        valueEventListener?.let { dbRef?.removeEventListener(it) }
        userRef?.setValue(false)
        valueEventListener = null
        dbRef = null
        userRef = null
    }

    // Optional for manually update before logout user
    // because on logout firebase database Permission denied error comes
    fun setOnlineStatus(online: Boolean) {
        userRef?.setValue(online)
    }

}

class FirebaseQueryValueObserver {
    private var valueEventListener: ValueEventListener? = null
    private var query: Query? = null

    fun start(valueEventListener: ValueEventListener, query: Query) {
        query.addListenerForSingleValueEvent(valueEventListener)
        this.valueEventListener = valueEventListener
        this.query = query
    }

    fun clear(b: (() -> Unit)? = null) {
        valueEventListener?.let { query?.removeEventListener(it) }
        valueEventListener = null
        query = null
        b?.invoke()
    }
}

class FirebaseReferenceValueObserver {
    private var valueEventListener: ValueEventListener? = null
    private var dbRef: DatabaseReference? = null

    fun start(valueEventListener: ValueEventListener, reference: DatabaseReference) {
        reference.addValueEventListener(valueEventListener)
        this.valueEventListener = valueEventListener
        this.dbRef = reference
    }

    fun clear() {
        valueEventListener?.let { dbRef?.removeEventListener(it) }
        valueEventListener = null
        dbRef = null
    }
}

class FirebaseReferenceChildObserver {
    private var valueEventListener: ChildEventListener? = null
    private var dbRef: DatabaseReference? = null
    private var isObserving: Boolean = false

    fun start(valueEventListener: ChildEventListener, reference: DatabaseReference) {
        isObserving = true
        reference.addChildEventListener(valueEventListener)
        this.valueEventListener = valueEventListener
        this.dbRef = reference
    }

    fun clear() {
        valueEventListener?.let { dbRef?.removeEventListener(it) }
        isObserving = false
        valueEventListener = null
        dbRef = null
    }

    fun isObserving(): Boolean {
        return isObserving
    }
}

// Task based
class FirebaseDataSource {

    //region Private

    private fun refToPath(path: String): DatabaseReference = database.child(path)

    private fun attachValueListenerToTaskCompletion(src: TaskCompletionSource<DataSnapshot>): ValueEventListener {
        return (object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                src.setException(Exception(error.message))
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                src.setResult(snapshot)
            }
        })
    }

    private fun <T> attachValueListenerToBlock(
        resultClassName: Class<T>,
        b: ((Result<T>) -> Unit)
    ): ValueEventListener {
        return (object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                b.invoke(Result.Error(error.message))
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (wrapSnapshotToClass(resultClassName, snapshot) == null) {
                    b.invoke(Result.Error(msg = snapshot.key))
                } else {
                    b.invoke(Result.Success(wrapSnapshotToClass(resultClassName, snapshot)))
                }
            }
        })
    }

    private fun <T> attachValueListenerToBlockWithList(
        resultClassName: Class<T>,
        b: ((Result<MutableList<T>>) -> Unit)
    ): ValueEventListener {
        return (object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                b.invoke(Result.Error(error.message))
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                b.invoke(Result.Success(wrapSnapshotToArrayList(resultClassName, snapshot)))
            }
        })
    }

    private fun <T> attachChildListenerToBlock(
        resultClassName: Class<T>,
        b: ((Result<T>) -> Unit)
    ): ChildEventListener {
        return (object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                b.invoke(Result.Success(wrapSnapshotToClass(resultClassName, snapshot)))
            }

            override fun onCancelled(error: DatabaseError) {
                b.invoke(Result.Error(error.message))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    //endregion

    //region Upload
    fun uploadFcmToken(userID: String, token: String): Task<Unit> {
        return refToPath("tokens/$userID").setValue(token).toUnit()
    }
    //endregion

    //region Update
    fun updateUserEmail(userID: String, email: String): Task<Unit> {
        return refToPath("$REF_USERS_ROOT/$userID/$REF_USER_INFO").child("email").setValue(email)
            .toUnit()
    }

    fun updateUserPassword(userID: String, password: String): Task<Unit> {
        return refToPath("$REF_USERS_ROOT/$userID/$REF_USER_INFO").child("password")
            .setValue(password).toUnit()
    }

    fun updateUserProfile(userID: String, userData: Map<String, Any?>): Task<Unit> {
        return refToPath("$REF_USERS_ROOT/$userID/$REF_USER_INFO").updateChildren(userData).toUnit()
    }

    fun updateUserImage(userID: String, userImage: UserImage): Task<Unit> {
        return refToPath("$REF_USERS_ROOT/$userID/$REF_USER_IMAGES").push().setValue(userImage).toUnit()
    }

    fun updateNewFriend(myUser: UserFriend, otherUser: UserFriend) {
        refToPath("$REF_USERS_ROOT/${myUser.userID}/friends/${otherUser.userID}").setValue(otherUser)
        refToPath("$REF_USERS_ROOT/${otherUser.userID}/friends/${myUser.userID}").setValue(myUser)
    }

    fun updateNewSentRequest(userID: String, userRequest: UserRequest) {
        refToPath("$REF_USERS_ROOT/${userID}/sentRequests/${userRequest.userID}").setValue(
            userRequest
        )
    }

    fun updateNewNotification(otherUserID: String, userNotification: UserNotification) {
        refToPath("$REF_USERS_ROOT/${otherUserID}/notifications/${userNotification.userID}").setValue(
            userNotification
        )
    }

    fun updateNewUser(user: UserInfo, b: (() -> Unit)? = null) {
        refToPath("$REF_USERS_ROOT/${user.uid}/$REF_USER_INFO").setValue(user).addOnCompleteListener {
            b?.invoke()
        }
    }

    //endregion

    //region Remove

    fun removeNotification(userID: String, notificationID: String) {
        refToPath("$REF_USERS_ROOT/${userID}/notifications/$notificationID").setValue(null)
    }

    fun removeFriend(userID: String, friendID: String) {
        refToPath("$REF_USERS_ROOT/${userID}/friends/$friendID").setValue(null)
        refToPath("$REF_USERS_ROOT/${friendID}/friends/$userID").setValue(null)
    }

    fun removeSentRequest(userID: String, sentRequestID: String) {
        refToPath("$REF_USERS_ROOT/${userID}/sentRequests/$sentRequestID").setValue(null)
    }

    fun removeUserImage(userID: String, userImage: UserImage, b: (() -> Unit)?) {
        val query = refToPath("$REF_USERS_ROOT/$userID/$REF_USER_IMAGES")
            .orderByChild("imageUrl").equalTo(userImage.imageUrl)
        query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.elementAt(0).ref.removeValue()
                b?.invoke()
            }
            override fun onCancelled(error: DatabaseError) { b?.invoke() }
        })
    }

    //endregion

    //region Load
    fun getFcmToken(userID: String): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath("tokens/$userID").addListenerForSingleValueEvent(listener)
        return src.task
    }

    fun checkUsernameTask(username: String): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        database.child(REF_USERS_ROOT).orderByChild("$REF_USER_INFO/username")
            .equalTo(username).addListenerForSingleValueEvent(listener)
        return src.task
    }

    fun loadUserTask(userID: String): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath("$REF_USERS_ROOT/$userID").addListenerForSingleValueEvent(listener)
        return src.task
    }

    fun loadUserInfoTask(userID: String): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath("$REF_USERS_ROOT/$userID/$REF_USER_INFO").addListenerForSingleValueEvent(
            listener
        )
        return src.task
    }

    fun loadUsersTask(): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath(REF_USERS_ROOT).addListenerForSingleValueEvent(listener)
        return src.task
    }

    fun loadImagesTask(userID: String): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath("$REF_USERS_ROOT/$userID/$REF_USER_IMAGES").addListenerForSingleValueEvent(
            listener
        )
        return src.task
    }

    fun loadFriendsTask(userID: String): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath("$REF_USERS_ROOT/$userID/friends").addListenerForSingleValueEvent(listener)
        return src.task
    }

    fun loadChatTask(chatID: String): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath("chats/$chatID").addListenerForSingleValueEvent(listener)
        return src.task
    }

    fun loadNotificationsTask(userID: String): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath("$REF_USERS_ROOT/$userID/notifications").addListenerForSingleValueEvent(listener)
        return src.task
    }

    fun loadConfig(): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath("config").addListenerForSingleValueEvent(listener)
        return src.task
    }

    fun loadCallEventTask(userID: String): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath("calls/$userID").addListenerForSingleValueEvent(listener)
        return src.task
    }

    //endregion

    //region Value Observers

    fun <T> attachUserObserver(
        resultClassName: Class<T>,
        userID: String,
        refObs: FirebaseReferenceValueObserver,
        b: ((Result<T>) -> Unit)
    ) {
        val listener = attachValueListenerToBlock(resultClassName, b)
        refObs.start(listener, refToPath("$REF_USERS_ROOT/$userID"))
    }

    fun <T> attachUsersObserver(
        resultClassName: Class<T>,
        refObs: FirebaseReferenceValueObserver,
        b: ((Result<MutableList<T>>) -> Unit)
    ) {
        val listener = attachValueListenerToBlockWithList(resultClassName, b)
        refObs.start(listener, refToPath(REF_USERS_ROOT))
    }

    fun <T> attachUserInfoObserver(
        resultClassName: Class<T>,
        userID: String,
        refObs: FirebaseReferenceValueObserver,
        b: ((Result<T>) -> Unit)
    ) {
        val listener = attachValueListenerToBlock(resultClassName, b)
        refObs.start(listener, refToPath("$REF_USERS_ROOT/$userID/$REF_USER_INFO"))
    }

    fun <T> attachUserNotificationsObserver(
        resultClassName: Class<T>,
        userID: String,
        firebaseReferenceValueObserver: FirebaseReferenceValueObserver,
        b: ((Result<MutableList<T>>) -> Unit)
    ) {
        val listener = attachValueListenerToBlockWithList(resultClassName, b)
        firebaseReferenceValueObserver.start(
            listener,
            refToPath("$REF_USERS_ROOT/$userID/notifications")
        )
    }

    fun <T> attachUserFriendsObserver(
        resultClassName: Class<T>,
        userID: String,
        firebaseReferenceValueObserver: FirebaseReferenceValueObserver,
        b: ((Result<MutableList<T>>) -> Unit)
    ) {
        val listener = attachValueListenerToBlockWithList(resultClassName, b)
        firebaseReferenceValueObserver.start(
            listener,
            refToPath("$REF_USERS_ROOT/$userID/friends")
        )
    }

    fun <T> attachConfigObserver(
        resultClassName: Class<T>,
        refObs: FirebaseReferenceValueObserver,
        b: ((Result<T>) -> Unit)
    ) {
        val listener = attachValueListenerToBlock(resultClassName, b)
        refObs.start(listener, refToPath("config"))
    }

    //endregion
}
