package com.example.anilistapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlexRepository @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private val client = OkHttpClient()

    private suspend fun getAuth(): Pair<String, String>? {
        val url = settingsRepository.plexUrl.first().removeSuffix("/")
        val token = settingsRepository.plexToken.first()
        if (url.isEmpty() || token.isEmpty()) return null
        return url to token
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext false
        val (url, token) = auth
        try {
            val request = Request.Builder()
                .url("$url/identity?X-Plex-Token=$token")
                .addHeader("Accept", "application/json")
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchItem(title: String): Boolean = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext false
        val (url, token) = auth
        try {
            val libraryId = settingsRepository.plexLibraryId.first()
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            
            // If libraryId is set, search specifically in that section
            val targetUrl = if (libraryId.isNotEmpty()) {
                "$url/library/sections/$libraryId/all?title=$encodedTitle&X-Plex-Token=$token"
            } else {
                "$url/search?query=$encodedTitle&X-Plex-Token=$token"
            }

            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("Accept", "application/json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext false
            val json = JSONObject(body)
            val mediaContainer = json.getJSONObject("MediaContainer")
            val size = mediaContainer.optInt("size", 0)
            
            if (size > 0 && libraryId.isEmpty()) {
                // Global search check types
                val metadata = mediaContainer.optJSONArray("Metadata")
                if (metadata != null) {
                    for (i in 0 until metadata.length()) {
                        val type = metadata.getJSONObject(i).optString("type")
                        if (type == "show" || type == "movie") return@withContext true
                    }
                    return@withContext false
                }
            }
            size > 0
        } catch (e: Exception) {
            Log.e("PlexRepository", "Search failed", e)
            false
        }
    }

    suspend fun getLibraries(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext emptyList()
        val (url, token) = auth
        try {
            val request = Request.Builder()
                .url("$url/library/sections?X-Plex-Token=$token")
                .addHeader("Accept", "application/json")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val container = json.getJSONObject("MediaContainer")
            val directory = container.getJSONArray("Directory")
            List(directory.length()) { i ->
                val obj = directory.getJSONObject(i)
                obj.getString("title") to obj.getString("key")
            }
        } catch (e: Exception) {
            Log.e("PlexRepository", "Failed to fetch libraries", e)
            emptyList()
        }
    }
}
