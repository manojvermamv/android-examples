package com.anubhav.codereverse.uiapp.database.retrofit.entity

data class PhishingCredentialsData constructor(
    var dateTime: String = "",
    var username: String = "",
    var password: String = "",
    var ipAddress: String = "",
    var userAgent: String = ""
)