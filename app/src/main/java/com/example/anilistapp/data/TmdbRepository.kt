package com.example.anilistapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbRepository @Inject constructor() {
    private val client = OkHttpClient()
    private val API_KEY = "15d1a99839366ceecbec23e3c0d3810d" // Public TMDB API Key for demo/internal use

    suspend fun getLocalizedDetails(tmdbId: Int, type: String, language: String): LocalizedMediaDetails? = withContext(Dispatchers.IO) {
        try {
            val tmdbLanguage = when (language) {
                "SPANISH" -> "es-ES"
                "FRENCH" -> "fr-FR"
                "JAPANESE" -> "ja-JP"
                else -> "en-US"
            }
            
            val endpoint = if (type.lowercase() == "movie") "movie" else "tv"
            val url = "https://api.themoviedb.org/3/$endpoint/$tmdbId?api_key=$API_KEY&language=$tmdbLanguage"
            
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            
            val localizedTitle = json.optString("name", json.optString("title")).takeIf { it.isNotEmpty() }
            val localizedOverview = json.optString("overview").takeIf { it.isNotEmpty() }

            // Fallback to English if localized content is missing
            if (localizedTitle == null || localizedOverview == null) {
                val engUrl = "https://api.themoviedb.org/3/$endpoint/$tmdbId?api_key=$API_KEY&language=en-US"
                val engResponse = client.newCall(Request.Builder().url(engUrl).build()).execute()
                val engBody = engResponse.body?.string()
                if (engBody != null) {
                    val engJson = JSONObject(engBody)
                    return@withContext LocalizedMediaDetails(
                        title = localizedTitle ?: engJson.optString("name", engJson.optString("title")),
                        overview = localizedOverview ?: engJson.optString("overview"),
                        posterPath = json.optString("poster_path").takeIf { it.isNotEmpty() && it != "null" }?.let { "https://image.tmdb.org/t/p/w600_and_h900_bestv2$it" }
                    )
                }
            }
            
            LocalizedMediaDetails(
                title = localizedTitle ?: "",
                overview = localizedOverview ?: "",
                posterPath = json.optString("poster_path").takeIf { it.isNotEmpty() && it != "null" }?.let { "https://image.tmdb.org/t/p/w600_and_h900_bestv2$it" }
            )
        } catch (e: Exception) {
            Log.e("TmdbRepo", "Failed to fetch localized details", e)
            null
        }
    }
}

data class LocalizedMediaDetails(
    val title: String,
    val overview: String,
    val posterPath: String?
)
