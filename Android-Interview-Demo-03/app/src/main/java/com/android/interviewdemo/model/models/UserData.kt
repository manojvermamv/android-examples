package com.android.interviewdemo.model.models

import com.google.gson.annotations.SerializedName


data class UserData (
  @SerializedName("user_matches"        ) var userMatches        : UserMatches?        = UserMatches(),
  @SerializedName("current_offers"      ) var currentOffers      : CurrentOffers?      = CurrentOffers(),
  @SerializedName("featured_tournament" ) var featuredTournament : FeaturedTournament? = FeaturedTournament(),
  @SerializedName("upcoming_matches"    ) var upcomingMatches    : UpcomingMatches?    = UpcomingMatches(),
  @SerializedName("wallet_summary"      ) var walletSummary      : WalletSummary?      = WalletSummary()
)

data class UserMatches (
  @SerializedName("match_list" ) var matchList : ArrayList<UserMatchesList> = arrayListOf()
)

data class CurrentOffers (
  @SerializedName("offer_list" ) var offerList : ArrayList<OfferList> = arrayListOf()
)

data class FeaturedTournament (
  @SerializedName("tournament_list" ) var tournamentList : ArrayList<TournamentList> = arrayListOf()
)

data class UpcomingMatches (
  @SerializedName("match_list"    ) var matchList     : ArrayList<Match>  = arrayListOf(),
  @SerializedName("next_page"     ) var nextPage     : Int?              = null,
  @SerializedName("prev_page"     ) var prev_page     : Int?              = null,
  @SerializedName("previous_page" ) var previous_page : Int?              = null,
)  {

    // previous_page -> prev_page
    fun prevPage(): Int? = previous_page ?: prev_page

}

data class WalletSummary(
  @SerializedName("total_balance" ) var totalBalance : Int? = null,
  @SerializedName("cash_balance"  ) var cashBalance  : Int? = null,
  @SerializedName("bonus_balance" ) var bonusBalance : Int? = null
)