package com.example.anilistapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KitsuRepository @Inject constructor() {
    private val client = OkHttpClient()

    suspend fun searchMedia(query: String): List<KitsuSearchResult> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val request = Request.Builder()
                .url("https://kitsu.io/api/edge/anime?filter[text]=$encodedQuery&page[limit]=10")
                .addHeader("Accept", "application/vnd.api+json")
                .addHeader("Content-Type", "application/vnd.api+json")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val data = JSONObject(body).getJSONArray("data")
            
            List(data.length()) { i ->
                val obj = data.getJSONObject(i)
                val attributes = obj.getJSONObject("attributes")
                KitsuSearchResult(
                    id = obj.getString("id"),
                    title = attributes.optString("canonicalTitle", "Unknown"),
                    synopsis = attributes.optString("synopsis", "No synopsis available."),
                    posterUrl = attributes.getJSONObject("posterImage").optString("large", ""),
                    youtubeVideoId = attributes.optString("youtubeVideoId", ""),
                    isAnime = true // This is the /anime endpoint
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchManga(query: String): List<KitsuSearchResult> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val request = Request.Builder()
                .url("https://kitsu.io/api/edge/manga?filter[text]=$encodedQuery&page[limit]=10")
                .addHeader("Accept", "application/vnd.api+json")
                .addHeader("Content-Type", "application/vnd.api+json")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val data = JSONObject(body).getJSONArray("data")
            
            List(data.length()) { i ->
                val obj = data.getJSONObject(i)
                val attributes = obj.getJSONObject("attributes")
                KitsuSearchResult(
                    id = obj.getString("id"),
                    title = attributes.optString("canonicalTitle", "Unknown"),
                    synopsis = attributes.optString("synopsis", "No synopsis available."),
                    posterUrl = attributes.getJSONObject("posterImage").optString("large", ""),
                    youtubeVideoId = "",
                    isAnime = false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDetailsByTitle(title: String, isAnime: Boolean = true): KitsuSearchResult? = withContext(Dispatchers.IO) {
        val results = if (isAnime) searchMedia(title) else searchManga(title)
        // Find the closest match or just return the first one
        results.firstOrNull { it.title.equals(title, ignoreCase = true) } ?: results.firstOrNull()
    }
}

data class KitsuSearchResult(
    val id: String,
    val title: String,
    val synopsis: String,
    val posterUrl: String,
    val youtubeVideoId: String,
    val isAnime: Boolean
)
