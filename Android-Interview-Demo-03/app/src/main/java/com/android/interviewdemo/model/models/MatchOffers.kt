package com.android.interviewdemo.model.models

import com.google.gson.annotations.SerializedName


data class MatchOffers (
  @SerializedName("offer_icon_url" ) var offerIconUrl : String? = null,
  @SerializedName("title"          ) var title        : String? = null,
  @SerializedName("subtext"        ) var subtext      : String? = null
)