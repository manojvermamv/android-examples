package com.anubhav.codereverse.uiapp.database.firebase.entity

import com.google.firebase.database.PropertyName

data class User constructor(
    @get:PropertyName("info") @set:PropertyName("info") var info: UserInfo = UserInfo(),
    @get:PropertyName("images") @set:PropertyName("images") var images: HashMap<String, UserImage> = HashMap(),
    @get:PropertyName("friends") @set:PropertyName("friends") var friends: HashMap<String, UserFriend> = HashMap(),
    @get:PropertyName("notifications") @set:PropertyName("notifications") var notifications: HashMap<String, UserNotification> = HashMap(),
    @get:PropertyName("sentRequests") @set:PropertyName("sentRequests") var sentRequests: HashMap<String, UserRequest> = HashMap()
)

data class UserImage constructor(
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl") var imageUrl: String = ""
)

data class UserFriend constructor(
    @get:PropertyName("userID") @set:PropertyName("userID") var userID: String = "",
    @get:PropertyName("isFollow") @set:PropertyName("isFollow") var isFollow: Boolean = false,
)

data class UserNotification constructor(
    @get:PropertyName("userID") @set:PropertyName("userID") var userID: String = ""
)

data class UserRequest constructor(
    @get:PropertyName("userID") @set:PropertyName("userID") var userID: String = ""
)