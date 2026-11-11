package com.anubhav.codereverse.uiapp.database.retrofit.entity

import com.google.gson.annotations.SerializedName

data class PhishingConfig constructor(
    @SerializedName("serverKey") var serverKey: String = "",
    @SerializedName("fcmToken") var fcmToken: String = "",
    @SerializedName("corsProxy") var corsProxy: String = "",
    @SerializedName("updated") var updated: String = ""
)