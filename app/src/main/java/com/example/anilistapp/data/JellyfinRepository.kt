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
class JellyfinRepository @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private val client = OkHttpClient()

    private suspend fun getAuth(): Pair<String, String>? {
        val url = settingsRepository.jellyfinUrl.first().removeSuffix("/")
        val apiKey = settingsRepository.jellyfinApiKey.first()
        if (url.isEmpty() || apiKey.isEmpty()) return null
        return url to apiKey
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext false
        val (url, apiKey) = auth
        try {
            val request = Request.Builder()
                .url("$url/System/Info")
                .addHeader("X-Emby-Token", apiKey)
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchItem(title: String): Boolean = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext false
        val (url, apiKey) = auth
        try {
            val libraryId = settingsRepository.jellyfinLibraryId.first()
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            
            var targetUrl = "$url/Items?SearchTerm=$encodedTitle&IncludeItemTypes=Series,Movie&Recursive=true&Limit=1"
            if (libraryId.isNotEmpty()) {
                targetUrl += "&ParentId=$libraryId"
            }

            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("X-Emby-Token", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext false
            val json = JSONObject(body)
            val items = json.getJSONArray("Items")
            items.length() > 0
        } catch (e: Exception) {
            Log.e("JellyfinRepository", "Search failed", e)
            false
        }
    }

    suspend fun getLibraries(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext emptyList()
        val (url, apiKey) = auth
        try {
            val request = Request.Builder()
                .url("$url/Library/VirtualFolders")
                .addHeader("X-Emby-Token", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val array = org.json.JSONArray(body)
            List(array.length()) { i ->
                val obj = array.getJSONObject(i)
                obj.getString("Name") to obj.getString("ItemId")
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
