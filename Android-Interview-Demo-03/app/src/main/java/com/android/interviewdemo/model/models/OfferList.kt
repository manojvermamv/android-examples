package com.android.interviewdemo.model.models

import com.google.gson.annotations.SerializedName


data class OfferList (
  @SerializedName("offer_banner_url" ) var offerBannerUrl : String? = null,
  @SerializedName("type"             ) var type           : String? = null
)