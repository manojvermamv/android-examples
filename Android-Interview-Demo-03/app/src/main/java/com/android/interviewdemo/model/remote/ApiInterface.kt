package com.android.interviewdemo.model.remote

import com.android.interviewdemo.model.models.TournamentList
import com.android.interviewdemo.model.models.UpcomingMatches
import com.android.interviewdemo.model.models.UserData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface ApiInterface {

    @Headers("x-api-key:a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6")
    @GET("homepage/{uid}")
    fun getUserData(
        @Path("uid") uid: String
    ): Call<UserData>


    @GET("matches/featured/{page}")
    fun getFeaturedTournaments(
        @Path("page") page: String
    ): Call<TournamentList>


    @GET("matches/upcoming/{page}")
    fun getUpcomingMatches(
        @Path("page") page: String
    ): Call<UpcomingMatches>


}