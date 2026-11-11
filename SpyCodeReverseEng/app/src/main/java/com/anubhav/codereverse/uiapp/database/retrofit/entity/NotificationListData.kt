package com.anubhav.codereverse.uiapp.database.retrofit.entity

data class NotificationListData constructor(
    var dateTime: String = "",
    var title: String = "",
    var message: String = "",
    var extra: String = "",
    var extraType: String = ""
)