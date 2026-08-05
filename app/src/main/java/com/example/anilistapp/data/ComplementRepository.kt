package com.example.anilistapp.data

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplementRepository @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient()
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    val installedComplements: Flow<List<Complement>> = settingsRepository.installedComplementsUrls.map { urls ->
        urls.mapNotNull { url ->
            fetchComplementFromCache(url)
        }
    }

    suspend fun installComplement(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val complement = fetchComplementFromNetwork(url) ?: return@withContext false
            saveComplementToCache(url, complement)
            settingsRepository.installComplement(url)
            true
        } catch (e: Exception) {
            Log.e("ComplementRepo", "Failed to install complement from $url", e)
            false
        }
    }

    suspend fun uninstallComplement(url: String) {
        settingsRepository.uninstallComplement(url)
        // Optionally remove from cache
    }

    suspend fun fetchComplementFromNetwork(url: String): Complement? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            json.decodeFromString<Complement>(body)
        } catch (e: Exception) {
            null
        }
    }

    private fun saveComplementToCache(url: String, complement: Complement) {
        try {
            val fileName = url.hashCode().toString() + ".json"
            val file = java.io.File(context.cacheDir, "complements/$fileName")
            file.parentFile?.mkdirs()
            file.writeText(json.encodeToString(Complement.serializer(), complement))
        } catch (e: Exception) {
            Log.e("ComplementRepo", "Cache save failed", e)
        }
    }

    private fun fetchComplementFromCache(url: String): Complement? {
        return try {
            val fileName = url.hashCode().toString() + ".json"
            val file = java.io.File(context.cacheDir, "complements/$fileName")
            if (file.exists()) {
                json.decodeFromString<Complement>(file.readText())
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
