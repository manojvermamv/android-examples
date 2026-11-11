package com.anubhav.codereverse.uiapp.data.model

import com.google.gson.annotations.SerializedName

data class MaskingUrlData constructor(
    @SerializedName("originalUrl") var originalUrl: String = "",
    @SerializedName("maskingUrl") var maskingUrl: String = "",
    @SerializedName("dateTime") var dateTime: String = ""
)