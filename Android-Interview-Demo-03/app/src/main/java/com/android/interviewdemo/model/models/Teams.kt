package com.android.interviewdemo.model.models

import com.google.gson.annotations.SerializedName


data class Teams(
  @SerializedName("a" ) var a : Team? = Team(),
  @SerializedName("b" ) var b : Team? = Team()
)

data class Team(
  @SerializedName("name") var name: String? = null,
  @SerializedName("code") var code: String? = null,
  @SerializedName("logo_url") var logoUrl: String? = null,
  @SerializedName("logo_bg_color") var logoBgColor: String? = null
)