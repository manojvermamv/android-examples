package com.anubhav.codereverse.uiapp.database.firebase.entity

import com.anubhav.commonutility.HelperMethod
import com.anubhav.codereverse.R
import com.anubhav.codereverse.uiapp.ui.MyApp
import com.anubhav.codereverse.uiapp.utils.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.Exclude
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

data class UserInfo constructor(
    var uid: String = "",
    var fullName: String = "",
    var username: String = "",
    var email: String = "",
    var password: String = "",
    var profileUrl: String = "",
    var city: String = "",
    var country: String = "",
    var onlineStatus: Boolean = false,
    var isBanned: Boolean = false,
    var isFakeUser: Boolean = false,
    var lastSeen: String = "",
    var friends: HashMap<String, UserFriend> = HashMap(),
    var notifications: HashMap<String, UserNotification> = HashMap(),
    var sentRequests: HashMap<String, UserRequest> = HashMap()
) : Serializable {

    @Exclude
    @set:Exclude
    @get:Exclude
    var profileRes: Int = R.mipmap.img_avatar_placeholder

    @Exclude
    @set:Exclude
    @get:Exclude
    var isNewUser: Boolean = false

    // the map representation of this class
    fun asMap(): Map<String, Any> = filterNotNullValues(toMapWithOnlyPrimaryConstructor(this))

}

fun userInfoChanges(newUser: UserInfo, oldUser: UserInfo): Map<String, Any?> {
    val newMap = toMapWithOnlyPrimaryConstructor(newUser)
    val oldMap = toMapWithOnlyPrimaryConstructor(oldUser)
    return filterValuesChange(newMap, oldMap)
}

fun mkUser(email: String, displayName: String? = null, password: String? = null, imageUrl: String = ""): UserInfo {
    val mUser = UserInfo()
    mUser.uid = ""
    mUser.fullName = if (displayName.isNullOrEmpty()) mkDisplayName(email) else displayName
    mUser.username = mkUsername()
    mUser.email = email
    mUser.password = if (password.isNullOrEmpty()) mkPassword() else password
    mUser.profileUrl = imageUrl
    mUser.city = "None"
    mUser.country = MyApp.countryName
    mUser.onlineStatus = false // set online for user and go to to home page? ( not go to home page because email verification)
    return mUser
}

fun mkUser(user: FirebaseUser): UserInfo {
    val mUser = UserInfo()
    mUser.uid = user.uid
    mUser.fullName = user.displayName ?: ""
    mUser.username = mkUsername()
    mUser.email = user.email ?: ""
    mUser.password = mkPassword()
    mUser.profileUrl = user.photoUrl.toString()
    mUser.city = "None"
    mUser.country = MyApp.countryName
    mUser.onlineStatus = true // set online for user and go to to home page
    return mUser
}

private fun mkDisplayName(email: String): String {
    return email.split("@")[0]
}

private fun mkUsername() = "user${System.currentTimeMillis()}"

private fun mkPassword() = HelperMethod.generateRandomNumber().toString()