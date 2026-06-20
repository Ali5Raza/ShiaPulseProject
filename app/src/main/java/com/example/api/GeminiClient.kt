package com.example.api

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.BuildConfig

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    private var cachedQuotes: List<ShiaQuote>? = null
    private var cachedAyats: List<ShiaQuote>? = null

    suspend fun getDailyShiaQuote(context: android.content.Context, topic: String? = null): ShiaQuote = withContext(Dispatchers.IO) {
        if (cachedQuotes == null) {
            cachedQuotes = loadJsonList(context, "quotes.json")
        }
        val items = cachedQuotes ?: LocalQuotes.quotes
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val index = dayOfYear % items.size
        return@withContext items[index]
    }

    suspend fun getDailyQuranAyat(context: android.content.Context, topic: String? = null): ShiaQuote = withContext(Dispatchers.IO) {
        if (cachedAyats == null) {
            cachedAyats = loadJsonList(context, "ayats.json")
        }
        val items = cachedAyats ?: LocalAyats.ayats
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val index = dayOfYear % items.size
        return@withContext items[index]
    }

    private fun loadJsonList(context: android.content.Context, filename: String): List<ShiaQuote>? {
        return try {
            val jsonStr = context.assets.open(filename).bufferedReader().use { it.readText() }
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, ShiaQuoteJson::class.java)
            val adapter = moshi.adapter<List<ShiaQuoteJson>>(listType)
            val parsedList = adapter.fromJson(jsonStr)
            parsedList?.map { ShiaQuote(arabic = it.arabic, english = it.english, source = it.source, urdu = it.urdu) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}
data class ShiaQuoteJson(
    @Json(name = "arabic") val arabic: String = "",
    @Json(name = "english") val english: String = "",
    @Json(name = "source") val source: String = "",
    @Json(name = "urdu") val urdu: String = ""
)
