package com.example.anilistapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeerrRepository @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val titleCache = mutableMapOf<Int, String>()
    
    // Cached global status map using "type_id" as key
    private var cachedStatusMap: Map<String, Int>? = null
    private val titleIdMap = mutableMapOf<String, Pair<Int, String>>() // Clean Title -> (ID, Type)
    private var lastLibraryFetch = 0L
    private val CACHE_DURATION = 300_000L // 5 minutes

    private suspend fun getAuth(): Pair<String, String>? {
        val url = settingsRepository.seerrUrl.first().removeSuffix("/")
        val apiKey = settingsRepository.seerrApiKey.first()
        if (url.isEmpty() || apiKey.isEmpty()) {
            Log.w("SeerrRepository", "Auth failed: URL or API Key is empty. URL: '$url'")
            return null
        }
        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "http://$url"
        } else url
        return formattedUrl to apiKey
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext false
        val (url, apiKey) = auth
        try {
            val request = Request.Builder()
                .url("$url/api/v1/status")
                .addHeader("X-Api-Key", apiKey)
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchShow(title: String): List<SeerrSearchResult> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext emptyList()
        val (url, apiKey) = auth
        try {
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val request = Request.Builder()
                .url("$url/api/v1/search?query=$encodedTitle")
                .addHeader("X-Api-Key", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val results = JSONObject(body).getJSONArray("results")
            List(results.length()) { i ->
                val obj = results.getJSONObject(i)
                val type = obj.getString("mediaType")
                val tmdbId = obj.getInt("id")
                
                val mediaInfo = if (obj.has("mediaInfo") && !obj.isNull("mediaInfo")) obj.getJSONObject("mediaInfo") else null
                var status = mediaInfo?.optInt("status", 1)
                
                // Enrichment: Check our global library cache for the status of this search result
                if (status == null || status == 1) {
                    status = cachedStatusMap?.get("${type}_$tmdbId") ?: 1
                }
                
                SeerrSearchResult(
                    id = tmdbId,
                    title = obj.optString("name", obj.optString("title")),
                    type = type,
                    overview = obj.optString("overview"),
                    status = status
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllMediaStatuses(forceRefresh: Boolean = false): Map<String, Int> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cachedStatusMap != null && (now - lastLibraryFetch < CACHE_DURATION)) {
            return@withContext cachedStatusMap!!
        }

        val auth = getAuth() ?: return@withContext emptyMap()
        val (url, apiKey) = auth
        try {
            // Fetch a large batch to ensure we have everything
            val request = Request.Builder()
                .url("$url/api/v1/media?take=5000&filter=all")
                .addHeader("X-Api-Key", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyMap()
            val results = JSONObject(body).getJSONArray("results")
            val statusMap = mutableMapOf<String, Int>()
            for (i in 0 until results.length()) {
                val obj = results.getJSONObject(i)
                val tmdbId = obj.optInt("tmdbId", -1)
                val type = obj.optString("mediaType", "tv")
                val status = obj.optInt("status", -1)
                if (tmdbId != -1 && status != -1) {
                    statusMap["${type}_$tmdbId"] = status
                }
            }
            
            // Second pass: Populate Title Map for title-based cache lookup
            // We fetch specific details for available items to get titles
            // This is slow, so we only do it for items we might actually need to match
            
            cachedStatusMap = statusMap
            lastLibraryFetch = now
            statusMap
        } catch (e: Exception) {
            Log.e("SeerrRepository", "Error fetching all media statuses", e)
            cachedStatusMap ?: emptyMap()
        }
    }

    suspend fun getFullMediaLibrary(): List<SeerrMediaInfo> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext emptyList()
        val (url, apiKey) = auth
        try {
            val request = Request.Builder()
                .url("$url/api/v1/media?take=1000&filter=all")
                .addHeader("X-Api-Key", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val results = JSONObject(body).getJSONArray("results")
            List(results.length()) { i ->
                val obj = results.getJSONObject(i)
                SeerrMediaInfo(
                    tmdbId = obj.getInt("tmdbId"),
                    type = obj.getString("mediaType"),
                    status = obj.getInt("status")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMediaDetails(tmdbId: Int, type: String): SeerrSearchResult? = withContext(Dispatchers.IO) {
        // 1. Check Global Cache First (Highest Accuracy)
        val globalStatuses = getAllMediaStatuses()
        val cachedStatus = globalStatuses["${type}_$tmdbId"]
            ?: globalStatuses["tv_$tmdbId"]
            ?: globalStatuses["movie_$tmdbId"]

        val auth = getAuth() ?: return@withContext null
        val (url, apiKey) = auth
        try {
            val endpoint = if (type == "movie") "movie" else "tv"
            val request = Request.Builder()
                .url("$url/api/v1/$endpoint/$tmdbId")
                .addHeader("X-Api-Key", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val obj = JSONObject(body)
            
            val title = obj.optString("name", obj.optString("title"))
            if (title.isNotEmpty()) titleCache[tmdbId] = title

            // Check if mediaInfo exists directly on the object or if we need to check our cache
            var status: Int? = if (obj.has("mediaInfo") && !obj.isNull("mediaInfo")) {
                obj.getJSONObject("mediaInfo").optInt("status")
            } else null
            
            // Fallback: Check cached global media status map
            if (status == null || status == 1) {
                status = cachedStatus
            }

            SeerrSearchResult(
                id = obj.getInt("id"),
                title = title,
                type = type,
                overview = obj.optString("overview"),
                status = status
            )
        } catch (e: Exception) {
            // If the specific detail call fails, return result from cache only
            if (cachedStatus != null) {
                return@withContext SeerrSearchResult(
                    id = tmdbId,
                    title = titleCache[tmdbId] ?: "Unknown Item",
                    type = type,
                    overview = "Details currently offline, but status is known.",
                    status = cachedStatus
                )
            }
            null
        }
    }

    suspend fun findInCache(tmdbId: Int?, title: String): SeerrSearchResult? {
        val statuses = getAllMediaStatuses()
        if (tmdbId != null) {
            val status = statuses["tv_$tmdbId"] ?: statuses["movie_$tmdbId"]
            if (status != null) {
                return SeerrSearchResult(id = tmdbId, title = title, type = if (statuses.containsKey("tv_$tmdbId")) "tv" else "movie", overview = "", status = status)
            }
        }
        return null
    }

    suspend fun getShowDetails(tmdbId: Int, type: String): List<Int> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext emptyList()
        val (url, apiKey) = auth
        try {
            val endpoint = if (type == "tv") "tv" else "movie"
            val request = Request.Builder()
                .url("$url/api/v1/$endpoint/$tmdbId")
                .addHeader("X-Api-Key", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            if (type == "tv") {
                val seasons = JSONObject(body).getJSONArray("seasons")
                List(seasons.length()) { i -> seasons.getJSONObject(i).getInt("seasonNumber") }
            } else listOf(0)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getQualityProfiles(): List<SeerrProfile> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext emptyList()
        val (url, apiKey) = auth
        try {
            val request = Request.Builder()
                .url("$url/api/v1/settings/profiles")
                .addHeader("X-Api-Key", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            Log.d("SeerrRepository", "Global profiles response: $body")
            val profilesArray = try { JSONArray(body) } catch (e: Exception) {
                try { JSONObject(body).getJSONArray("profiles") } catch (e2: Exception) { null }
            }
            if (profilesArray != null) {
                List(profilesArray.length()) { i ->
                    val obj = profilesArray.getJSONObject(i)
                    SeerrProfile(id = obj.getInt("id"), name = obj.getString("name"))
                }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProfilesForServer(type: String, serverId: Int): List<SeerrProfile> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext emptyList()
        val (url, apiKey) = auth
        try {
            val endpoint = if (type == "movie") "radarr" else "sonarr"
            val targetUrl = "$url/api/v1/service/$endpoint/$serverId"
            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("X-Api-Key", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val obj = JSONObject(body)
            val profilesArray = obj.optJSONArray("qualityProfiles") ?: obj.optJSONArray("profiles") 
            if (profilesArray != null) {
                List(profilesArray.length()) { i ->
                    val p = profilesArray.getJSONObject(i)
                    SeerrProfile(id = p.getInt("id"), name = p.getString("name"))
                }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRadarrSettings(): List<SeerrServer> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext emptyList()
        val (url, apiKey) = auth
        try {
            val request = Request.Builder()
                .url("$url/api/v1/settings/radarr")
                .addHeader("X-Api-Key", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val array = try { JSONArray(body) } catch (e: Exception) { null }
            if (array != null) {
                List(array.length()) { i ->
                    val obj = array.getJSONObject(i)
                    SeerrServer(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        isDefault = obj.optBoolean("isDefault"),
                        activeProfileId = if (obj.has("activeProfileId")) obj.getInt("activeProfileId") else null,
                        activeProfileName = obj.optString("activeProfileName", "")
                    )
                }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSonarrSettings(): List<SeerrServer> = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext emptyList()
        val (url, apiKey) = auth
        try {
            val request = Request.Builder()
                .url("$url/api/v1/settings/sonarr")
                .addHeader("X-Api-Key", apiKey)
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            val array = try { JSONArray(body) } catch (e: Exception) { null }
            if (array != null) {
                List(array.length()) { i ->
                    val obj = array.getJSONObject(i)
                    SeerrServer(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        isDefault = obj.optBoolean("isDefault"),
                        activeProfileId = if (obj.has("activeProfileId")) obj.getInt("activeProfileId") else null,
                        activeProfileName = obj.optString("activeProfileName", "")
                    )
                }
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun requestMedia(
        tmdbId: Int,
        type: String,
        seasons: List<Int>,
        profileId: Int,
        serverId: Int? = null,
        rootFolder: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val auth = getAuth() ?: return@withContext false
        val (url, apiKey) = auth
        try {
            val json = JSONObject().apply {
                put("mediaId", tmdbId)
                put("mediaType", type)
                if (type == "tv") {
                    put("seasons", JSONArray(seasons))
                }
                put("profileId", profileId)
                if (serverId != null && serverId != -1) put("serverId", serverId)
                if (!rootFolder.isNullOrEmpty()) put("rootFolder", rootFolder)
            }
            val request = Request.Builder()
                .url("$url/api/v1/request")
                .post(json.toString().toRequestBody())
                .addHeader("X-Api-Key", apiKey)
                .addHeader("Content-Type", "application/json")
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

data class SeerrSearchResult(
    val id: Int,
    val title: String,
    val type: String,
    val overview: String,
    val status: Int? = null
)
data class SeerrProfile(val id: Int, val name: String)
data class SeerrServer(
    val id: Int, 
    val name: String, 
    val isDefault: Boolean,
    val activeProfileId: Int? = null,
    val activeProfileName: String? = null
)
data class SeerrMediaInfo(val tmdbId: Int, val type: String, val status: Int)
