package com.android.interviewdemo.model.repository

import android.util.Log
import com.android.interviewdemo.model.local.ContentDataSource
import com.android.interviewdemo.model.models.ContactItem
import com.android.interviewdemo.model.models.ErrorResponse
import com.android.interviewdemo.model.models.TournamentList
import com.android.interviewdemo.model.models.UpcomingMatches
import com.android.interviewdemo.model.models.UserData
import com.android.interviewdemo.model.remote.RetrofitClient
import com.android.interviewdemo.utils.Resource
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import retrofit2.awaitResponse

class MainRepository(private val source: ContentDataSource) {

    private val api = RetrofitClient.apiInterface

    suspend fun fetchContacts(): List<ContactItem> {
        return withContext(Dispatchers.IO) {
            source.fetchContacts()
        }
    }

    suspend fun getUserDataApiCall(): Resource<UserData> {
        return withContext(Dispatchers.IO) {
            delay(20)
            makeApiCall { api.getUserData("1") }
        }
    }

    suspend fun getFeaturedTournamentsApiCall(pageIndex: Int): Resource<TournamentList> {
        return withContext(Dispatchers.IO) {
            delay(if (pageIndex == 1) 20 else 250)
            makeApiCall { api.getFeaturedTournaments(pageIndex.toString()) }
        }
    }

    suspend fun getUpcomingMatchesApiCall(pageIndex: Int): Resource<UpcomingMatches> {
        return withContext(Dispatchers.IO) {
            delay(if (pageIndex == 1) 20 else 250)
            makeApiCall { api.getUpcomingMatches(pageIndex.toString()) }
        }
    }

}

/**
 * Helper function to make API call and handle exceptions
 * */
suspend inline fun <reified T> makeApiCall(apiCall: () -> Call<T>): Resource<T> {
    return try {
        val response = apiCall().awaitResponse()
        if (response.isSuccessful) {
            Resource.Success(response.body()!!)
        } else {
            // Handle API error (e.g., non-200 status code)
            Resource.Error(Exception(response.catchError()))
        }
    } catch (e: Exception) {
        // Handle network or other exceptions
        Log.d("DEBUG", "API error: ${e.message}")
        Resource.Error(e)
    }
}

inline fun <reified T> Response<T>.catchError(): String {
    return when (code()) {
        500 -> {
            val errorResponse = Gson().fromJson(errorBody()?.string(), ErrorResponse::class.java)
            Log.d("DEBUG", "Custom error ${errorResponse.status}: ${errorResponse.message}")
            errorResponse?.message ?: "Internal Server Error"
        }
        else -> {
            Log.d("DEBUG", "API error: ${code()} - ${message()}")
            message()
        }
    }
}