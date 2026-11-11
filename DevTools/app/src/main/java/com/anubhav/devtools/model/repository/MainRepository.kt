package com.anubhav.devtools.model.repository

import android.util.Log
import com.anubhav.devtools.model.models.Contact
import com.anubhav.devtools.model.models.Tool
import com.anubhav.devtools.model.models.User
import com.anubhav.devtools.model.retrofit.RetrofitClient
import com.anubhav.devtools.model.local.ContentDataSource
import com.anubhav.devtools.model.models.ToolNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.awaitResponse

class MainRepository(private val source: ContentDataSource) {

    private val api = RetrofitClient.apiInterface

    suspend fun fetchTools(): List<Tool> {
        return withContext(Dispatchers.IO) {
            ToolNames.entries.map {
                Tool(it.name, it.description, it.imageRes)
            }
        }
    }

    suspend fun fetchContacts(): List<Contact> {
        return withContext(Dispatchers.IO) {
            source.fetchContacts()
        }
    }

    suspend fun getUsersApiCall(): List<User> {
        return withContext(Dispatchers.IO) {
            makeApiCall { api.getUsers() } ?: emptyList()
        }
    }

}


suspend inline fun <reified T> makeApiCall(apiCall: () -> Call<T>): T? {
    return try {
        val response = apiCall().awaitResponse()
        if (response.isSuccessful) {
            response.body()
        } else {
            // Handle API error (e.g., non-200 status code)
            Log.e("DEBUG", "API error: ${response.code()} - ${response.message()}")
            null // Or throw an exception or return a default value
        }
    } catch (e: Exception) {
        // Handle network or other exceptions
        Log.e("DEBUG", "Error during API call: ${e.message}")
        null // Or throw an exception or return a default value
    }
}