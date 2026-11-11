package com.android.interviewdemo.ui.activities

import android.util.Log
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query
import okhttp3.OkHttpClient
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

/**
 * NSE Option Chain Data Fetcher for Android
 * Replicates the functionality of the Python script for fetching NSE option chain data
 */
class NSEOptionChain {

    companion object {
        private const val TAG = "NSEOptionChain"
        private const val BASE_URL = "https://www.nseindia.com/"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        private const val ACCEPT_LANGUAGE = "en-US,en;q=0.9"
        private const val ACCEPT_ENCODING = "gzip, deflate, br"

        // Strike price calculation
        fun roundNearest(x: Double, num: Int = 50): Int = (ceil(x / num) * num).toInt()
        fun nearestStrikeBnf(x: Double): Int = roundNearest(x, 100)
        fun nearestStrikeNf(x: Double): Int = roundNearest(x, 50)
    }

    // Data Classes for API Responses
    data class IndicesResponse(
        val data: List<IndexData>
    )

    data class IndexData(
        val index: String,
        val last: Double
    )

    data class OptionChainResponse(
        val records: Records
    )

    data class Records(
        val expiryDates: List<String>,
        val data: List<OptionData>
    )

    data class OptionData(
        val strikePrice: Double,
        val expiryDate: String,
        @SerializedName("CE") val ce: OptionDetails?,
        @SerializedName("PE") val pe: OptionDetails?
    )

    data class OptionDetails(
        val openInterest: Double
    )

    data class OIData(
        val strikePrice: Double,
        val ceOI: Double,
        val peOI: Double
    )

    data class SupportResistance(
        val support: Double,
        val resistance: Double
    )

    // Enhanced Cookie Management
    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val host = url.host
            if (cookieStore[host] == null) {
                cookieStore[host] = mutableListOf()
            }

            // Update existing cookies or add new ones
            cookies.forEach { newCookie ->
                val existingIndex = cookieStore[host]?.indexOfFirst {
                    it.name == newCookie.name
                }

                if (existingIndex != null && existingIndex >= 0) {
                    cookieStore[host]!![existingIndex] = newCookie
                } else {
                    cookieStore[host]?.add(newCookie)
                }
            }

            Log.d(TAG, "Saved ${cookies.size} cookies for $host. Total: ${cookieStore[host]?.size}")
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val cookies = cookieStore[url.host] ?: emptyList()
            //val cookies = cookieStore[url.host]?.filter { !it.hasExpired() } ?: emptyList()
            Log.d(TAG, "Loading ${cookies.size} cookies for ${url.host}")
            return cookies
        }

        fun clearCookies() {
            cookieStore.clear()
            Log.d(TAG, "Cleared all cookies")
        }

        fun getCookieCount(): Int = cookieStore.values.sumOf { it.size }
    }

    // Retrofit API Interface
    interface NSEApi {
        @GET("option-chain")
        @Headers(
            "User-Agent: $USER_AGENT",
            "Accept-Language: $ACCEPT_LANGUAGE",
            "Accept-Encoding: $ACCEPT_ENCODING",
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Connection: keep-alive"
        )
        suspend fun getOptionChainPage(): retrofit2.Response<okhttp3.ResponseBody>

        @GET("api/allIndices")
        suspend fun getAllIndices(): retrofit2.Response<okhttp3.ResponseBody>

        @GET("api/option-chain-indices")
        suspend fun getOptionChain(
            @Query("symbol") symbol: String
        ): retrofit2.Response<okhttp3.ResponseBody>
    }

    // Enhanced OkHttp Client with proper compression handling
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .cookieJar(cookieJar)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            // Add common headers to all api requests
            if (original.url.toString().contains("api/")) {
                requestBuilder.addHeader("User-Agent", USER_AGENT)
                requestBuilder.addHeader("Accept-Language", ACCEPT_LANGUAGE)
                requestBuilder.addHeader("Accept", "application/json")
                requestBuilder.addHeader("Content-Type", "application/json;charset=utf-8")
                requestBuilder.addHeader("Connection", "keep-alive")
            }
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .build()

    // Gson instance with lenient parsing
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val api = retrofit.create(NSEApi::class.java)

    // Global variables
    private var bnfUL: Double = 0.0
    private var nfUL: Double = 0.0
    private var bnfNearest: Int = 0
    private var nfNearest: Int = 0

    // Data storage for comparison
    private var prevNiftyData: List<OIData>? = null
    private var prevBankNiftyData: List<OIData>? = null

    /**
     * Enhanced cookie setting with proper session establishment
     */
    private suspend fun setCookies(): Boolean {
        return try {
            Log.d(TAG, "Setting cookies...")
            cookieJar.clearCookies()

            // First, visit the main page to get initial cookies
            val mainPageResponse = api.getOptionChainPage()

            if (mainPageResponse.isSuccessful) {
                Log.d(TAG, "Main page visited successfully. Cookies: ${cookieJar.getCookieCount()}")

                // Wait a bit to simulate human behavior
                delay(2000)

                // Make a test request to indices API to ensure session is established
                val testResponse = api.getAllIndices()

                if (testResponse.isSuccessful) {
                    Log.d(TAG, "Session established successfully")
                    delay(1000)
                    return true
                } else {
                    Log.w(TAG, "Test request failed with code: ${testResponse.code()}")
                    // Still return true if main page was successful
                    return true
                }
            } else {
                Log.e(TAG, "Failed to visit main page: ${mainPageResponse.code()}")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting cookies", e)
            false
        }
    }

    /**
     * Enhanced JSON response parsing with proper decompression
     */
    private inline fun <reified T> parseJsonResponse(responseBody: okhttp3.ResponseBody?): T? {
        return try {
            responseBody?.let { body ->
                // The response body is automatically decompressed by OkHttp
                val jsonString = body.string()

                // Check if we got a valid JSON response
                if (jsonString.isBlank()) {
                    Log.e(TAG, "Empty response received")
                    return null
                }

                // Log first 200 characters for debugging
                Log.d(TAG, "Response preview: ${jsonString.take(200)}...")

                // Check if response is HTML (error page)
                if (jsonString.trim().startsWith("<")) {
                    Log.e(TAG, "Received HTML instead of JSON - possible authentication issue")
                    return null
                }

                // Check if response is valid JSON
                if (!jsonString.trim().startsWith("{") && !jsonString.trim().startsWith("[")) {
                    Log.e(TAG, "Response doesn't appear to be valid JSON")
                    return null
                }

                gson.fromJson(jsonString, T::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON response", e)
            null
        }
    }

    /**
     * Enhanced data fetching with better error handling and retry logic
     */
    private suspend fun fetchAllData(): Triple<IndicesResponse?, OptionChainResponse?, OptionChainResponse?> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch indices data first
                val indicesResponse = try {
                    Log.d(TAG, "Fetching indices data...")
                    val response = api.getAllIndices()
                    Log.d(TAG, "Indices API response code: ${response.code()}")

                    if (response.isSuccessful) {
                        parseJsonResponse<IndicesResponse>(response.body())
                    } else {
                        Log.e(TAG, "Indices API failed: ${response.code()} - ${response.message()}")
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching indices", e)
                    null
                }

                // If indices failed, don't proceed with option chains
                if (indicesResponse == null) {
                    Log.e(TAG, "Indices data failed, skipping option chains")
                    return@withContext Triple(null, null, null)
                }

                // Small delay between requests
                delay(1000)

                // Fetch option chains concurrently
                val bnfDeferred = async {
                    try {
                        Log.d(TAG, "Fetching BankNifty option chain...")
                        val response = api.getOptionChain("BANKNIFTY")
                        Log.d(TAG, "BankNifty API response code: ${response.code()}")

                        if (response.isSuccessful) {
                            parseJsonResponse<OptionChainResponse>(response.body())
                        } else {
                            Log.e(TAG, "BankNifty API failed: ${response.code()} - ${response.message()}")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching BankNifty", e)
                        null
                    }
                }

                val nfDeferred = async {
                    try {
                        Log.d(TAG, "Fetching Nifty option chain...")
                        val response = api.getOptionChain("NIFTY")
                        Log.d(TAG, "Nifty API response code: ${response.code()}")

                        if (response.isSuccessful) {
                            parseJsonResponse<OptionChainResponse>(response.body())
                        } else {
                            Log.e(TAG, "Nifty API failed: ${response.code()} - ${response.message()}")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching Nifty", e)
                        null
                    }
                }

                val bnfResponse = bnfDeferred.await()
                val nfResponse = nfDeferred.await()

                Triple(indicesResponse, bnfResponse, nfResponse)
            } catch (e: Exception) {
                Log.e(TAG, "Error in fetchAllData", e)
                Triple(null, null, null)
            }
        }
    }

    /**
     * Process indices data to get underlying prices
     */
    private fun processIndicesData(data: IndicesResponse) {
        data.data.forEach { index ->
            when (index.index) {
                "NIFTY 50" -> {
                    nfUL = index.last
                    nfNearest = nearestStrikeNf(nfUL)
                    Log.d(TAG, "NIFTY 50: $nfUL, Nearest: $nfNearest")
                }
                "NIFTY BANK" -> {
                    bnfUL = index.last
                    bnfNearest = nearestStrikeBnf(bnfUL)
                    Log.d(TAG, "NIFTY BANK: $bnfUL, Nearest: $bnfNearest")
                }
            }
        }
    }

    /**
     * Enhanced process OI data with null safety
     */
    private fun processOIData(data: OptionChainResponse, nearest: Int, step: Int, num: Int): List<OIData> {
        val currExpiryDate = data.records.expiryDates.firstOrNull() ?: return emptyList()
        val oiData = mutableListOf<OIData>()

        val rangeStart = nearest - step * num
        val rangeEnd = nearest + step * num

        data.records.data
            .filter { it.expiryDate == currExpiryDate }
            .filter { it.strikePrice >= rangeStart && it.strikePrice <= rangeEnd }
            .sortedBy { it.strikePrice }
            .forEach { item ->
                oiData.add(
                    OIData(
                        strikePrice = item.strikePrice,
                        ceOI = item.ce?.openInterest ?: 0.0,
                        peOI = item.pe?.openInterest ?: 0.0
                    )
                )
            }

        Log.d(TAG, "Processed ${oiData.size} OI data points for range $rangeStart-$rangeEnd")
        return oiData
    }

    /**
     * Calculate support and resistance levels based on highest OI
     */
    private fun calculateSupportResistance(oiData: List<OIData>): SupportResistance {
        val highestCEOI = oiData.maxByOrNull { it.ceOI }
        val highestPEOI = oiData.maxByOrNull { it.peOI }

        return SupportResistance(
            support = highestPEOI?.strikePrice ?: 0.0,
            resistance = highestCEOI?.strikePrice ?: 0.0
        )
    }

    /**
     * Print OI data with changes
     */
    private fun printOIData(
        niftyData: List<OIData>,
        bankNiftyData: List<OIData>,
        prevNiftyData: List<OIData>?,
        prevBankNiftyData: List<OIData>?
    ) {
        Log.d(TAG, "=== Nifty Open Interest ===")
        niftyData.forEachIndexed { index, data ->
            val ceChange = if (prevNiftyData != null && index < prevNiftyData.size) {
                data.ceOI - prevNiftyData[index].ceOI
            } else 0L

            val peChange = if (prevNiftyData != null && index < prevNiftyData.size) {
                data.peOI - prevNiftyData[index].peOI
            } else 0L

            Log.d(TAG, "Strike: ${data.strikePrice}, CE OI: ${data.ceOI} ($ceChange), PE OI: ${data.peOI} ($peChange)")
        }

        Log.d(TAG, "=== Bank Nifty Open Interest ===")
        bankNiftyData.forEachIndexed { index, data ->
            val ceChange = if (prevBankNiftyData != null && index < prevBankNiftyData.size) {
                data.ceOI - prevBankNiftyData[index].ceOI
            } else 0L

            val peChange = if (prevBankNiftyData != null && index < prevBankNiftyData.size) {
                data.peOI - prevBankNiftyData[index].peOI
            } else 0L

            Log.d(TAG, "Strike: ${data.strikePrice}, CE OI: ${data.ceOI} ($ceChange), PE OI: ${data.peOI} ($peChange)")
        }
    }

    /**
     * Main update cycle with enhanced retry logic
     */
    suspend fun startDataUpdates(
        onDataUpdate: (
            niftyData: List<OIData>,
            bankNiftyData: List<OIData>,
            niftySR: SupportResistance,
            bankNiftySR: SupportResistance,
            prevNiftyData: List<OIData>?,
            prevBankNiftyData: List<OIData>?
        ) -> Unit,
        intervalSeconds: Int = 30
    ) {
        var retryCount = 0
        val maxRetries = 5
        var sessionEstablished = false

        while (true) {
            try {
                // Establish session if not already done or after multiple failures
                if (!sessionEstablished || retryCount >= 3) {
                    Log.d(TAG, "Establishing session...")
                    sessionEstablished = setCookies()

                    if (!sessionEstablished) {
                        Log.e(TAG, "Failed to establish session, retry ${retryCount + 1}/$maxRetries")
                        retryCount++
                        if (retryCount >= maxRetries) {
                            Log.e(TAG, "Max retries reached for session establishment")
                            delay(120000) // Wait 2 minutes before trying again
                            retryCount = 0
                            continue
                        }
                        delay(10000)
                        continue
                    }
                }

                Log.d(TAG, "Fetching new data...")
                val (indicesData, bnfData, nfData) = fetchAllData()

                if (indicesData == null || bnfData == null || nfData == null) {
                    Log.e(TAG, "Failed to fetch data, retry ${retryCount + 1}/$maxRetries")
                    retryCount++

                    // Reset session after multiple failures
                    if (retryCount >= 3) {
                        sessionEstablished = false
                    }

                    if (retryCount >= maxRetries) {
                        Log.e(TAG, "Max retries reached for data fetching")
                        delay(120000) // Wait 2 minutes before trying again
                        retryCount = 0
                        sessionEstablished = false
                        continue
                    }
                    delay(15000)
                    continue
                }

                // Reset retry count on successful data fetch
                retryCount = 0

                // Process indices data
                processIndicesData(indicesData)

                // Process option chain data
                val niftyOIData = processOIData(nfData, nfNearest, 50, 10)
                val bankNiftyOIData = processOIData(bnfData, bnfNearest, 100, 10)

                // Calculate support and resistance
                val niftySR = calculateSupportResistance(niftyOIData)
                val bankNiftySR = calculateSupportResistance(bankNiftyOIData)

                Log.d(TAG, "Nifty S/R: Support=${niftySR.support}, Resistance=${niftySR.resistance}")
                Log.d(TAG, "Bank Nifty S/R: Support=${bankNiftySR.support}, Resistance=${bankNiftySR.resistance}")

                // Print data
                printOIData(niftyOIData, bankNiftyOIData, prevNiftyData, prevBankNiftyData)

                // Callback with updated data
                onDataUpdate(
                    niftyOIData,
                    bankNiftyOIData,
                    niftySR,
                    bankNiftySR,
                    prevNiftyData,
                    prevBankNiftyData
                )

                // Store current data for next comparison
                prevNiftyData = niftyOIData
                prevBankNiftyData = bankNiftyOIData

                // Wait for next update
                repeat(intervalSeconds) { i ->
                    Log.d(TAG, "Next update in ${intervalSeconds - i} seconds...")
                    delay(1000)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in data update cycle", e)
                retryCount++
                sessionEstablished = false

                if (retryCount >= maxRetries) {
                    Log.e(TAG, "Max retries reached in update cycle")
                    delay(120000) // Wait 2 minutes before trying again
                    retryCount = 0
                } else {
                    delay(15000) // Wait 15 seconds before retry
                }
            }
        }
    }

    /**
     * Enhanced one-time data fetch
     */
    suspend fun fetchCurrentData(): Pair<List<OIData>, List<OIData>>? {
        return try {
            var retryCount = 0
            val maxRetries = 2

            while (retryCount < maxRetries) {
                if (!setCookies()) {
                    Log.e(TAG, "Failed to set cookies, retry ${retryCount + 1}/$maxRetries")
                    retryCount++
                    delay(5000)
                    continue
                }

                val (indicesData, bnfData, nfData) = fetchAllData()

                if (indicesData == null || bnfData == null || nfData == null) {
                    Log.e(TAG, "Failed to fetch data, retry ${retryCount + 1}/$maxRetries")
                    retryCount++
                    delay(10000)
                    continue
                }

                processIndicesData(indicesData)

                val niftyOIData = processOIData(nfData, nfNearest, 50, 10)
                val bankNiftyOIData = processOIData(bnfData, bnfNearest, 100, 10)

                return Pair(niftyOIData, bankNiftyOIData)
            }

            Log.e(TAG, "Max retries reached in fetchCurrentData")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching current data", e)
            null
        }
    }

    /**
     * Clear cookies
     */
    fun clearCookies() {
        cookieJar.clearCookies()
    }

    /**
     * Enhanced test method with better debugging
     */
    suspend fun testApiResponses(): String {
        val result = StringBuilder()

        try {
            // Test cookie setting
            result.append("1. Testing cookie setting...\n")
            val cookieSuccess = setCookies()
            result.append("Cookie setting: ${if (cookieSuccess) "SUCCESS" else "FAILED"}\n")
            result.append("Cookies stored: ${cookieJar.getCookieCount()}\n\n")

            if (!cookieSuccess) {
                result.append("Stopping tests due to cookie failure\n")
                return result.toString()
            }

            // Test indices API
            result.append("2. Testing indices API...\n")
            try {
                val indicesResponse = api.getAllIndices()
                result.append("Indices API Status: ${indicesResponse.code()}\n")
                result.append("Indices API Message: ${indicesResponse.message()}\n")

                if (indicesResponse.isSuccessful) {
                    val body = indicesResponse.body()?.string()
                    result.append("Indices Response Length: ${body?.length ?: 0}\n")

                    // Check if response looks like JSON
                    if (body != null && body.trim().startsWith("{")) {
                        result.append("Indices Response: Valid JSON format\n")
                        result.append("Indices Response Preview: ${body.take(100)}...\n")
                    } else {
                        result.append("Indices Response: Not valid JSON\n")
                        result.append("Indices Response Preview: ${body?.take(100)}\n")
                    }
                } else {
                    result.append("Indices API Failed\n")
                }
                result.append("\n")
            } catch (e: Exception) {
                result.append("Indices API Error: ${e.message}\n\n")
            }

            // Test Nifty option chain API
            result.append("3. Testing Nifty option chain API...\n")
            try {
                val niftyResponse = api.getOptionChain("NIFTY")
                result.append("Nifty API Status: ${niftyResponse.code()}\n")
                result.append("Nifty API Message: ${niftyResponse.message()}\n")

                if (niftyResponse.isSuccessful) {
                    val body = niftyResponse.body()?.string()
                    result.append("Nifty Response Length: ${body?.length ?: 0}\n")

                    // Check if response looks like JSON
                    if (body != null && body.trim().startsWith("{")) {
                        result.append("Nifty Response: Valid JSON format\n")
                        result.append("Nifty Response Preview: ${body.take(100)}...\n")
                    } else {
                        result.append("Nifty Response: Not valid JSON\n")
                        result.append("Nifty Response Preview: ${body?.take(100)}\n")
                    }
                } else {
                    result.append("Nifty API Failed\n")
                }
                result.append("\n")
            } catch (e: Exception) {
                result.append("Nifty API Error: ${e.message}\n\n")
            }

            // Test BankNifty option chain API
            result.append("4. Testing BankNifty option chain API...\n")
            try {
                val bankNiftyResponse = api.getOptionChain("BANKNIFTY")
                result.append("BankNifty API Status: ${bankNiftyResponse.code()}\n")
                result.append("BankNifty API Message: ${bankNiftyResponse.message()}\n")

                if (bankNiftyResponse.isSuccessful) {
                    val body = bankNiftyResponse.body()?.string()
                    result.append("BankNifty Response Length: ${body?.length ?: 0}\n")

                    if (body != null && body.trim().startsWith("{")) {
                        result.append("BankNifty Response: Valid JSON format\n")
                        result.append("BankNifty Response Preview: ${body.take(100)}...\n")
                    } else {
                        result.append("BankNifty Response: Not valid JSON\n")
                        result.append("BankNifty Response Preview: ${body?.take(100)}\n")
                    }
                } else {
                    result.append("BankNifty API Failed\n")
                }
            } catch (e: Exception) {
                result.append("BankNifty API Error: ${e.message}\n")
            }

            return result.toString()

        } catch (e: Exception) {
            return "Test failed with error: ${e.message}"
        }
    }
}