package com.android.interviewdemo.model.models

import com.google.gson.annotations.SerializedName


data class Metadata (
  @SerializedName("is_lineup_out"        ) var isLineupOut        : Boolean? = null,
  @SerializedName("is_match_initialized" ) var isMatchInitialized : Boolean? = null
)