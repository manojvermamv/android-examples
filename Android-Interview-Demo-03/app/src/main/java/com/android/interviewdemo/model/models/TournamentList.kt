package com.android.interviewdemo.model.models

import com.google.gson.annotations.SerializedName

data class TournamentList(
    @SerializedName("tournament_id"     ) var tournamentId  : String?           = null,
    @SerializedName("tournament_name"   ) var tournamentName: String?           = null,
    @SerializedName("match_list"        ) var matchList     : ArrayList<Match>  = arrayListOf(),
    @SerializedName("next_page"         ) var nextPage     : Int?              = null,
    @SerializedName("prev_page"         ) var prev_page     : Int?              = null,
    @SerializedName("previous_page"     ) var previous_page : Int?              = null,
) {

    // previous_page -> prev_page
    fun prevPage(): Int? = previous_page ?: prev_page

}