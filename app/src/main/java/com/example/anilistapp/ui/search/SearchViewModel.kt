package com.example.anilistapp.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilistapp.data.KitsuRepository
import com.example.anilistapp.data.KitsuSearchResult
import com.example.anilistapp.data.SeerrRepository
import com.example.anilistapp.data.SeerrSearchResult
import com.example.anilistapp.data.MediaRepository
import com.example.anilistapp.data.CustomSource
import com.example.anilistapp.data.ComplementRepository
import com.example.anilistapp.ui.components.SoundManager
import com.example.anilistapp.SearchAniListQuery
import com.example.anilistapp.type.MediaListStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val kitsuResults: List<KitsuSearchResult> = emptyList(),
    val aniListResults: List<SearchAniListQuery.Medium> = emptyList(),
    val groupedAniListResults: Map<String, List<SearchAniListQuery.Medium>> = emptyMap(),
    val seerrResults: List<SeerrSearchResult> = emptyList(),
    val customResults: Map<String, List<KitsuSearchResult>> = emptyMap(),
    val customSources: List<CustomSource> = emptyList(),
    val isLoading: Boolean = false,
    val showSearchTags: Boolean = true,
    val groupSeasons: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val kitsuRepository: KitsuRepository,
    private val seerrRepository: SeerrRepository,
    private val mediaRepository: MediaRepository,
    private val complementRepository: ComplementRepository,
    private val settingsRepository: com.example.anilistapp.data.SettingsRepository,
    val soundManager: SoundManager
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.showSearchTags.collect { show ->
                _state.value = _state.value.copy(showSearchTags = show)
            }
        }
        viewModelScope.launch {
            settingsRepository.groupSeasons.collect { group ->
                _state.value = _state.value.copy(groupSeasons = group)
                if (_state.value.aniListResults.isNotEmpty()) {
                    updateGroupedResults(_state.value.aniListResults)
                }
            }
        }
        viewModelScope.launch {
            complementRepository.installedComplements.collect { complements ->
                val sources = complements.flatMap { it.searchProviders }
                _state.value = _state.value.copy(customSources = sources)
            }
        }
    }

    fun onQueryChanged(query: String) {
        _state.value = _state.value.copy(query = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.length > 2) {
                delay(500)
                _state.value = _state.value.copy(isLoading = true)
                
                // Search all sources
                val kitsuAnime = kitsuRepository.searchMedia(query)
                val kitsuManga = kitsuRepository.searchManga(query)
                val kitsu = kitsuAnime + kitsuManga

                val aniList = try {
                    val res = mediaRepository.searchAniList(query)
                    res.data?.Page?.media?.filterNotNull() ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                val seerr = try {
                    seerrRepository.searchShow(query)
                } catch (e: Exception) {
                    emptyList()
                }
                
                val customRes = mutableMapOf<String, List<KitsuSearchResult>>()
                _state.value.customSources.forEach { source ->
                    try {
                        val results = searchCustomSource(source, query)
                        if (results.isNotEmpty()) {
                            customRes[source.name] = results
                        }
                    } catch (e: Exception) {
                        Log.e("SearchVM", "Custom source ${source.name} failed", e)
                    }
                }

                _state.value = _state.value.copy(
                    kitsuResults = kitsu,
                    aniListResults = aniList,
                    isLoading = false
                )
                updateGroupedResults(aniList)
                
                _state.value = _state.value.copy(
                    seerrResults = seerr,
                    customResults = customRes
                )
            } else {
                _state.value = _state.value.copy(kitsuResults = emptyList(), seerrResults = emptyList())
            }
        }
    }

    private suspend fun searchCustomSource(source: CustomSource, query: String): List<KitsuSearchResult> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val url = source.searchUrl.replace("%s", java.net.URLEncoder.encode(query, "UTF-8"))
            val request = okhttp3.Request.Builder().url(url).build()
            val client = okhttp3.OkHttpClient()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()
            
            val json = org.json.JSONObject(body)
            val results = if (source.resultsPath.contains(".")) {
                var current: Any = json
                source.resultsPath.split(".").forEach { key ->
                    current = (current as org.json.JSONObject).get(key)
                }
                current as org.json.JSONArray
            } else {
                json.getJSONArray(source.resultsPath)
            }
            
            List(results.length()) { i ->
                val obj = results.getJSONObject(i)
                KitsuSearchResult(
                    id = i.toString(),
                    title = resolvePath(obj, source.titlePath),
                    synopsis = resolvePath(obj, source.summaryPath),
                    posterUrl = resolvePath(obj, source.posterPath),
                    youtubeVideoId = "",
                    isAnime = true
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun resolvePath(obj: org.json.JSONObject, path: String): String {
        return try {
            var current: Any = obj
            val parts = path.split(".")
            parts.dropLast(1).forEach { key ->
                current = (current as org.json.JSONObject).get(key)
            }
            (current as org.json.JSONObject).optString(parts.last(), "")
        } catch (e: Exception) {
            ""
        }
    }

    fun addToWatchlist(title: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(message = "Searching for AniList ID...")
                val aniSearch = mediaRepository.searchAniList(title)
                val id = aniSearch.data?.Page?.media?.firstOrNull()?.id
                if (id != null) {
                    mediaRepository.saveMediaListEntry(id, MediaListStatus.PLANNING)
                    _state.value = _state.value.copy(message = "Added to Watchlist!")
                } else {
                    _state.value = _state.value.copy(message = "Could not find on AniList.")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(message = "Error: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun updateGroupedResults(results: List<SearchAniListQuery.Medium>) {
        if (!_state.value.groupSeasons) {
            _state.value = _state.value.copy(groupedAniListResults = emptyMap())
            return
        }

        val grouped = results.groupBy { media ->
            val title = media.title?.userPreferred ?: ""
            val isMovieFamily = media.format == com.example.anilistapp.type.MediaFormat.MOVIE || 
                               media.format == com.example.anilistapp.type.MediaFormat.OVA ||
                               media.format == com.example.anilistapp.type.MediaFormat.SPECIAL
            
            // Keep Movies separate even if they share a base title
            val groupingFormat = if (isMovieFamily) "MOVIE_${media.id}" else "TV"
            "${normalizeTitle(title)}|${media.type}|$groupingFormat"
        }
        _state.value = _state.value.copy(groupedAniListResults = grouped)
    }

    private fun normalizeTitle(title: String): String {
        // More robust normalization
        return title
            .replace(Regex("(?i)\\s+season\\s+\\d+.*"), "")
            .replace(Regex("(?i)\\s+part\\s+\\d+.*"), "")
            .replace(Regex("(?i)\\s+cour\\s+\\d+.*"), "")
            .replace(Regex("(?i)\\s+\\d+$"), "") // Remove trailing digits
            .replace(Regex("(?i)\\s+\\d+th$"), "") // e.g. 2nd, 3rd
            .replace(Regex("(?i)\\s+\\d+nd$"), "")
            .replace(Regex("(?i)\\s+\\d+rd$"), "")
            .replace(Regex("(?i)\\s+\\d+st$"), "")
            .trim()
    }
}
