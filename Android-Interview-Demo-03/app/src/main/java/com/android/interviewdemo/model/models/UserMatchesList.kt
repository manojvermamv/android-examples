package com.android.interviewdemo.model.models

import com.google.gson.annotations.SerializedName

data class UserMatchesList (
    @SerializedName("match"            ) var match          : Match? = Match(),
    @SerializedName("user_contests"    ) var userContests   : Int?   = null,
    @SerializedName("user_teams"       ) var userTeams      : Int?   = null,
    @SerializedName("top_running_rank" ) var topRunningRank : Int?   = null
)