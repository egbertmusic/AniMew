package com.example.anilistapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KitsuRepository @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            private val cookieStore = mutableMapOf<String, List<Cookie>>()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: listOf()
            }
        })
        .build()

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
        
        // Priority 1: Match by title exactly
        var match = results.find { it.title.equals(title, ignoreCase = true) } ?: results.firstOrNull()
        
        // Priority 2: Fallback to Jikan (MyAnimeList) if it's an anime and Kitsu didn't give a good trailer
        if (isAnime && (match == null || match.youtubeVideoId.isEmpty())) {
            try {
                val encodedQuery = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                val request = Request.Builder()
                    .url("https://api.jikan.moe/v4/anime?q=$encodedQuery&limit=1")
                    .build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                val data = JSONObject(body).optJSONArray("data")
                if (data != null && data.length() > 0) {
                    val anime = data.getJSONObject(0)
                    val trailer = anime.optJSONObject("trailer")
                    val youtubeId = trailer?.optString("youtube_id", "") ?: ""
                    if (youtubeId.isNotEmpty()) {
                        return@withContext KitsuSearchResult(
                            id = anime.getInt("mal_id").toString(),
                            title = anime.optString("title"),
                            synopsis = anime.optString("synopsis"),
                            posterUrl = anime.getJSONObject("images").getJSONObject("jpg").optString("large_image_url"),
                            youtubeVideoId = youtubeId,
                            isAnime = true
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("KitsuRepo", "Jikan fallback failed", e)
            }
        }
        
        match
    }

    suspend fun searchYouTubeTrailer(
        title: String, 
        language: String, 
        type: String = "ANIME", 
        format: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val languageKeywords = when (language) {
                "SPANISH" -> "(español OR castellano OR latino OR \"sub español\")"
                "FRENCH" -> "(français OR \"vostfr\")"
                "ENGLISH" -> "(english OR \"sub english\" OR dub)"
                else -> ""
            }
            
            val typeKeywords = when {
                format == "MOVIE" -> "pelicula trailer"
                format == "NOVEL" -> "light novel PV"
                type == "MANGA" -> "manga PV"
                else -> "anime trailer"
            }
            
            val query = "$title $typeKeywords $languageKeywords"
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            
            Log.d("KitsuRepo", "Deep Search Query: $query")

            val request = Request.Builder()
                .url("https://www.youtube.com/results?search_query=$encodedQuery&sp=EgIQAQ%3D%3D") // sp=EgIQAQ== filters for videos only
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Accept-Language", "en-US,en;q=0.9")
                .build()
            
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext null
            
            // We look for the first videoId that appears after the "search results" start
            // and try to avoid channel icons or other irrelevant links.
            val regex = Regex("\"videoId\":\"([a-zA-Z0-9_-]{11})\"")
            val matches = regex.findAll(html).toList()
            
            // Usually the first few are the most relevant search results
            return@withContext matches.firstOrNull()?.groupValues?.get(1)
        } catch (e: Exception) {
            Log.e("KitsuRepo", "YouTube deep search failed", e)
            null
        }
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
