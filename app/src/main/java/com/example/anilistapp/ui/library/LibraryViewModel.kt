package com.example.anilistapp.ui.library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anilistapp.data.MediaRepository
import com.example.anilistapp.data.SeerrRepository
import com.example.anilistapp.data.SettingsRepository
import com.example.anilistapp.data.JellyfinRepository
import com.example.anilistapp.data.PlexRepository
import com.example.anilistapp.GetUserListQuery
import com.example.anilistapp.type.MediaListStatus
import com.example.anilistapp.type.MediaType
import com.example.anilistapp.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryState(
    val isLoading: Boolean = false,
    val mediaList: List<GetUserListQuery.MediaList> = emptyList(),
    val error: String? = null,
    val selectedType: MediaType = MediaType.ANIME,
    val selectedStatus: MediaListStatus = MediaListStatus.CURRENT,
    val requestMessage: String? = null,
    val disableAnimeUpdate: Boolean = false,
    val disableMangaUpdate: Boolean = false,
    val isSeerrConfigured: Boolean = false,
    val showSeerrCloudInLibrary: Boolean = true,
    val enableMediaServerFallback: Boolean = true,
    val autoAddDownloadedToWatchlist: Boolean = false,
    val seerrMediaStatus: Map<Int, Int> = emptyMap(), // AniList ID to Seerr Status
    val titleLanguage: String = "ROMAJI",
    val showMultipleTitles: Boolean = false,
    val appLanguages: Set<String> = setOf("ENGLISH"),
    val randomizeUiLanguage: Boolean = false,
    val themeMode: AppTheme = AppTheme.DARK,
    val manualAvailableIds: Set<Int> = emptySet()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MediaRepository,
    private val seerrRepository: SeerrRepository,
    private val settingsRepository: SettingsRepository,
    private val jellyfinRepository: JellyfinRepository,
    private val plexRepository: PlexRepository,
    val localizationManager: com.example.anilistapp.ui.components.LocalizationManager
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private var userId: Int? = null
    private val fullAniListIds = mutableSetOf<Int>()

    init {
        fetchSettings()
        fetchViewerAndList(forceRefresh = false)
    }

    private fun fetchSettings() {
        viewModelScope.launch {
            settingsRepository.disableAnimeUpdate.collect { disable ->
                _state.update { it.copy(disableAnimeUpdate = disable) }
            }
        }
        viewModelScope.launch {
            settingsRepository.disableMangaUpdate.collect { disable ->
                _state.update { it.copy(disableMangaUpdate = disable) }
            }
        }
        viewModelScope.launch {
            settingsRepository.titleLanguage.collect { lang ->
                _state.update { it.copy(titleLanguage = lang) }
            }
        }
        viewModelScope.launch {
            settingsRepository.showMultipleTitles.collect { show ->
                _state.update { it.copy(showMultipleTitles = show) }
            }
        }
        viewModelScope.launch {
            settingsRepository.appLanguages.collect { langs ->
                _state.update { it.copy(appLanguages = langs) }
            }
        }
        viewModelScope.launch {
            settingsRepository.randomizeUiLanguage.collect { randomize ->
                _state.update { it.copy(randomizeUiLanguage = randomize) }
            }
        }
        viewModelScope.launch {
            settingsRepository.showSeerrCloudInLibrary.collect { show ->
                _state.update { it.copy(showSeerrCloudInLibrary = show) }
            }
        }
        viewModelScope.launch {
            settingsRepository.enableMediaServerFallback.collect { enable ->
                _state.update { it.copy(enableMediaServerFallback = enable) }
            }
        }
        viewModelScope.launch {
            settingsRepository.autoAddDownloadedToWatchlist.collect { autoAdd ->
                _state.update { it.copy(autoAddDownloadedToWatchlist = autoAdd) }
            }
        }
        viewModelScope.launch {
            settingsRepository.themeMode.collect { mode ->
                try {
                    _state.update { it.copy(themeMode = AppTheme.valueOf(mode)) }
                } catch (e: Exception) {
                    _state.update { it.copy(themeMode = AppTheme.DARK) }
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.manualAvailableIds.collect { ids ->
                _state.update { it.copy(manualAvailableIds = ids) }
            }
        }
        viewModelScope.launch {
            combine(
                settingsRepository.seerrUrl,
                settingsRepository.enableSeerr
            ) { url, enabled ->
                url.isNotEmpty() && enabled
            }.collect { configured ->
                _state.update { it.copy(isSeerrConfigured = configured) }
            }
        }
    }

    private fun fetchViewerAndList(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                var attempts = 0
                var viewerResponse: com.apollographql.apollo.api.ApolloResponse<com.example.anilistapp.GetViewerQuery.Data>? = null
                
                while (attempts < 3) {
                    try {
                        viewerResponse = repository.getViewer()
                        if (viewerResponse.data?.Viewer != null) break
                    } catch (e: Exception) {
                        Log.w("LibraryVM", "Viewer fetch attempt ${attempts + 1} failed")
                    }
                    attempts++
                    kotlinx.coroutines.delay(1000L)
                }

                if (viewerResponse == null || viewerResponse.hasErrors() || viewerResponse.data?.Viewer == null) {
                    _state.update { it.copy(isLoading = false, error = "AniList: ${viewerResponse?.errors?.firstOrNull()?.message ?: "Session Expired or Profile Error"}") }
                    return@launch
                }
                
                val viewerId = viewerResponse.data?.Viewer?.id
                if (viewerId != null) {
                    userId = viewerId
                    fetchList(viewerId, _state.value.selectedType, _state.value.selectedStatus, forceRefresh)
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to fetch viewer profile") }
                }
            } catch (e: Exception) {
                Log.e("LibraryVM", "Viewer fetch critical failed", e)
                _state.update { it.copy(isLoading = false, error = "Network error: Check connection") }
            }
        }
    }

    private suspend fun fetchList(id: Int, type: MediaType, status: MediaListStatus, forceRefresh: Boolean = false) {
        try {
            val response = repository.getUserList(id, type, status, forceRefresh)
            val list = response.data?.Page?.mediaList?.filterNotNull() ?: emptyList()
            
            // Track all IDs seen so far to avoid redundant auto-adds
            list.forEach { it.media?.id?.let { id -> fullAniListIds.add(id) } }
            
            _state.update { it.copy(isLoading = false, mediaList = list, error = null) }
            
            if (type == MediaType.ANIME && _state.value.isSeerrConfigured) {
                fetchSeerrStatus(list)
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    private var lastSyncTime = 0L
    private var syncJob: kotlinx.coroutines.Job? = null
    private fun fetchSeerrStatus(list: List<GetUserListQuery.MediaList>) {
        val now = System.currentTimeMillis()
        if (now - lastSyncTime < 60000) return 
        lastSyncTime = now
        
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            try {
                // 1. Batch fetch all known media statuses from Seerr
                val seerrStatusMap = seerrRepository.getAllMediaStatuses()
                val currentStatusMap = _state.value.seerrMediaStatus.toMutableMap()
                val itemsToSearch = mutableListOf<GetUserListQuery.MediaList>()

                list.forEach { entry ->
                    val media = entry.media ?: return@forEach
                    val tmdbId = media.externalLinks?.filterNotNull()
                        ?.find { it.site.contains("TMDB", ignoreCase = true) }
                        ?.url?.removeSuffix("/")?.split("/")?.lastOrNull()?.toIntOrNull()

                    val targetType = if (media.format?.name == "MOVIE") "movie" else "tv"
                    
                    var foundStatus: Int? = null
                    if (tmdbId != null) {
                        foundStatus = seerrStatusMap["${targetType}_$tmdbId"]
                            ?: seerrStatusMap["tv_$tmdbId"]
                            ?: seerrStatusMap["movie_$tmdbId"]
                    }

                    if (foundStatus != null) {
                        currentStatusMap[media.id] = foundStatus
                    } else if (!currentStatusMap.containsKey(media.id)) {
                        itemsToSearch.add(entry)
                    }
                }
                
                // Update state once for ID matches
                _state.update { it.copy(seerrMediaStatus = HashMap(currentStatusMap)) }

                // 2. Second pass: Search by Title (batched to reduce recompositions)
                if (itemsToSearch.isNotEmpty()) {
                    val batchSize = 5
                    itemsToSearch.chunked(batchSize).forEach { batch ->
                        val batchResults = mutableMapOf<Int, Int>()
                        batch.forEach { entry ->
                            try {
                                val media = entry.media ?: return@forEach
                                val targetSeerrType = if (media.format?.name == "MOVIE") "movie" else "tv"
                                
                                val titlesToTry = mutableListOf<String>()
                                // PRIORITY: English title first for media servers, then others
                                media.title?.english?.let { titlesToTry.add(it) }
                                media.title?.userPreferred?.let { titlesToTry.add(it) }
                                media.title?.romaji?.let { titlesToTry.add(it) }
                                
                                for (searchTitle in titlesToTry.distinct()) {
                                    if (searchTitle.isEmpty()) continue
                                    val results = seerrRepository.searchShow(searchTitle)
                                    
                                    // STRICT MATCHING: Force type match to avoid Anime vs Movie mixups
                                    val match = results.find { result ->
                                        isTitleMatch(searchTitle, result.title) && result.type == targetSeerrType
                                    }
                                    
                                    if (match != null) {
                                        val status = match.status ?: seerrStatusMap["${match.type}_${match.id}"] ?: 1
                                        batchResults[media.id] = status
                                        break
                                    }
                                }
                                
                                // FORCE Media Server Check (Always check if Seerr doesn't say it's Available)
                                if ((!batchResults.containsKey(media.id) || batchResults[media.id]!! < 5) && _state.value.enableMediaServerFallback) {
                                    for (st in titlesToTry) {
                                        try {
                                            if (jellyfinRepository.searchItem(st) || plexRepository.searchItem(st)) {
                                                batchResults[media.id] = 6 // Force Available
                                                break
                                            }
                                        } catch (e: Exception) { }
                                    }
                                }
                            } catch (e: Exception) { }
                        }
                        
                        // Update state only AFTER a full batch is processed
                        if (batchResults.isNotEmpty()) {
                            currentStatusMap.putAll(batchResults)
                            _state.update { it.copy(seerrMediaStatus = HashMap(currentStatusMap)) }
                        }
                        kotlinx.coroutines.delay(800L)
                    }
                }

                // --- AUTO WATCHLIST SYNC ---
                if (_state.value.autoAddDownloadedToWatchlist) {
                    syncNewDownloadedContent()
                }
            } catch (e: Exception) {
                Log.e("LibraryVM", "Failed to fetch Seerr statuses", e)
            }
        }
    }

    private val negativeCache = mutableSetOf<Int>() // TMDB IDs that failed to match
    private val processedTmdbIds = mutableSetOf<Int>() // TMDB IDs already synced this session
    
    private fun syncNewDownloadedContent() {
        viewModelScope.launch {
            try {
                // 1. Get Seerr Library (Available items)
                val seerrLibrary = seerrRepository.getFullMediaLibrary()
                val availableItems = seerrLibrary.filter { it.status == 5 }
                
                // Get library filtering from settings
                val jLib = settingsRepository.jellyfinLibraryId.first()
                val pLib = settingsRepository.plexLibraryId.first()
                
                availableItems.forEach { seerrItem ->
                    if (negativeCache.contains(seerrItem.tmdbId) || processedTmdbIds.contains(seerrItem.tmdbId)) {
                        return@forEach
                    }

                    try {
                        val details = seerrRepository.getMediaDetails(seerrItem.tmdbId, seerrItem.type)
                        if (details != null) {
                            // EXTRA VALIDATION: If library filtering is on, skip if not found in specific library
                            // This helps avoid adding non-anime movies (like Diary of a Wimpy Kid)
                            if (jLib.isNotEmpty() || pLib.isNotEmpty()) {
                                // Double check if it exists in the user's specific anime libraries
                                val foundLocally = (jLib.isNotEmpty() && jellyfinRepository.searchItem(details.title)) ||
                                                (pLib.isNotEmpty() && plexRepository.searchItem(details.title))
                                
                                if (!foundLocally) {
                                    Log.d("LibraryVM", "Skipping Seerr item not in selected libraries: ${details.title}")
                                    negativeCache.add(seerrItem.tmdbId)
                                    return@forEach
                                }
                            }

                            // Search AniList
                            val searchType = MediaType.ANIME
                            val aniSearch = repository.searchAniList(details.title, searchType)
                            val results = aniSearch.data?.Page?.media?.filterNotNull() ?: emptyList()
                            
                            val topResult = results.find { res ->
                                val isTypeMatch = if (seerrItem.type == "movie") res.format?.name == "MOVIE" else res.format?.name != "MOVIE"
                                val titles = listOfNotNull(res.title?.userPreferred, res.title?.romaji, res.title?.english)
                                // STRICT FORMAT: Only match if formats align (TV vs Movie)
                                isTypeMatch && titles.any { isTitleMatch(details.title, it) }
                            }

                            if (topResult != null) {
                                if (topResult.mediaListEntry == null && !fullAniListIds.contains(topResult.id)) {
                                    repository.saveMediaListEntry(topResult.id, MediaListStatus.PLANNING)
                                    Log.i("LibraryVM", "Successfully auto-added to Planning: ${details.title}")
                                    processedTmdbIds.add(seerrItem.tmdbId)
                                    fullAniListIds.add(topResult.id)
                                } else {
                                    // Already in list, mark as processed to stop searching
                                    processedTmdbIds.add(seerrItem.tmdbId)
                                }
                            } else {
                                Log.w("LibraryVM", "Could not find AniList match for: ${details.title}")
                                negativeCache.add(seerrItem.tmdbId)
                            }
                        }
                        // Small delay between searches to be nice to AniList
                        kotlinx.coroutines.delay(1000L)
                    } catch (e: Exception) {
                        Log.e("LibraryVM", "Failed item sync: ${seerrItem.tmdbId}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("LibraryVM", "Watchlist sync critical fail", e)
            }
        }
    }

    private fun isTitleMatch(aniTitle: String, seerrTitle: String): Boolean {
        // Clean titles: lowercase, remove non-alphanumeric, and remove common suffixes/prefixes
        fun clean(t: String) = t.lowercase()
            .replace(Regex("(?i)season \\d+"), "")
            .replace(Regex("(?i)part \\d+"), "")
            .replace(Regex("(?i)\\(tv\\)"), "")
            .replace(Regex("(?i)the movie"), "")
            .replace(Regex("[^a-z0-9]"), "")
            .trim()

        val s1 = clean(aniTitle)
        val s2 = clean(seerrTitle)
        
        if (s1.isEmpty() || s2.isEmpty()) return false
        
        // 1. Exact match after cleaning
        if (s1 == s2) return true
        
        // 2. Prevent matching short fragments
        if (s1.length < 4 || s2.length < 4) return false
        
        // 3. Containment check with length ratio (prevents "A" matching "Avatar")
        val ratio = if (s1.length > s2.length) s2.length.toFloat() / s1.length else s1.length.toFloat() / s2.length
        
        return (s1.contains(s2) || s2.contains(s1)) && ratio > 0.4f
    }

    fun onTypeSelected(type: MediaType) {
        _state.update { it.copy(selectedType = type, isLoading = true) }
        userId?.let { id ->
            viewModelScope.launch {
                fetchList(id, type, _state.value.selectedStatus)
            }
        }
    }

    fun onStatusSelected(status: MediaListStatus) {
        _state.update { it.copy(selectedStatus = status, isLoading = true) }
        userId?.let { id ->
            viewModelScope.launch {
                fetchList(id, _state.value.selectedType, status)
            }
        }
    }

    fun refresh() {
        lastSyncTime = 0L 
        fetchViewerAndList(forceRefresh = true)
    }

    fun updateProgress(mediaId: Int, newProgress: Int) {
        val type = _state.value.selectedType
        val isDisabled = if (type == MediaType.ANIME) _state.value.disableAnimeUpdate else _state.value.disableMangaUpdate
        if (isDisabled) return
        
        viewModelScope.launch {
            try {
                repository.updateProgress(mediaId, newProgress)
                userId?.let { fetchList(it, _state.value.selectedType, _state.value.selectedStatus) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Update failed: ${e.message}") }
            }
        }
    }

    fun toggleManualAvailable(mediaId: Int) {
        viewModelScope.launch {
            settingsRepository.toggleManualAvailable(mediaId)
        }
    }

    fun removeFromLibrary(mediaId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteMediaListEntry(mediaId)
                refresh()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Delete failed: ${e.message}") }
            }
        }
    }

    fun updateMediaStatus(mediaId: Int, status: MediaListStatus) {
        viewModelScope.launch {
            try {
                repository.saveMediaListEntry(mediaId, status)
                refresh()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Move failed: ${e.message}") }
            }
        }
    }

    fun clearRequestMessage() {
        _state.update { it.copy(requestMessage = null) }
    }
}
